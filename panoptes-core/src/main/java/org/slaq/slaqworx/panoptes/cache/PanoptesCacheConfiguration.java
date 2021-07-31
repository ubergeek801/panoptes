package org.slaq.slaqworx.panoptes.cache;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ManagedContext;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.map.MapStore;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.HazelcastCacheMetrics;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.data.HazelcastMapStoreFactory;
import org.slaq.slaqworx.panoptes.data.SecurityAttributeLoader;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.util.ApplicationContextAware;

/**
 * A Micronaut {@link Factory} that provides {@link Bean}s related to the Hazelcast cache.
 *
 * @author jeremy
 */
@Factory
public class PanoptesCacheConfiguration {
  /**
   * Creates a new {@link PanoptesCacheConfiguration}. Restricted because instances of this class
   * should be obtained through the {@link ApplicationContext} (if it is needed at all).
   */
  protected PanoptesCacheConfiguration() {
    // nothing to do
  }

  /**
   * Provides a {@link MapConfig} for the specified map and adds it to the given Hazelcast {@link
   * Config}.
   *
   * @param cacheName
   *     the name of the map/cache being created
   * @param mapStoreFactory
   *     the {@link HazelcastMapStoreFactory} to use to create the {@link MapStore}, or {@code null}
   *     to use no {@link MapStore}
   *
   * @return a {@link MapConfig} configured for the given map
   */
  protected MapConfig createMapConfiguration(String cacheName,
      HazelcastMapStoreFactory mapStoreFactory) {
    NearCacheConfig nearCacheConfig =
        new NearCacheConfig().setName("near-" + cacheName).setInMemoryFormat(InMemoryFormat.OBJECT)
            .setCacheLocalEntries(true);
    nearCacheConfig.getEvictionConfig().setEvictionPolicy(EvictionPolicy.NONE)
        .setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT).setSize(200_000);

    MapConfig mapConfig = new MapConfig(cacheName).setBackupCount(2).setReadBackupData(true)
        .setInMemoryFormat(InMemoryFormat.BINARY).setNearCacheConfig(nearCacheConfig);

    if (mapStoreFactory != null) {
      MapStoreConfig mapStoreConfig =
          new MapStoreConfig().setFactoryImplementation(mapStoreFactory).setWriteDelaySeconds(15)
              .setWriteBatchSize(1000).setInitialLoadMode(InitialLoadMode.LAZY);
      mapConfig.setMapStoreConfig(mapStoreConfig);
    }

    return mapConfig;
  }

  /**
   * Provides a Hazelcast configuration suitable for the detected runtime environment.
   *
   * @param securityAttributeLoader
   *     the {@link SecurityAttributeLoader} used to initialize {@link SecurityAttribute}s
   * @param portfolioMapConfig
   *     the {@link MapConfig} to use for {@link Portfolio} data
   * @param positionMapConfig
   *     the {@link MapConfig} to use for {@link Position} data
   * @param securityMapConfig
   *     the {@link MapConfig} to use for {@link Security} data
   * @param ruleMapConfig
   *     the {@link MapConfig} to use for {@link Rule} data
   * @param serializationConfig
   *     the {@link SerializationConfig} to use for the cache
   * @param clusterExecutorConfig
   *     the {@link ExecutorConfig} to use for clustered execution
   * @param jetConfig
   *     the {@link JetConfig} from which to configure Jet, if used
   * @param applicationContext
   *     the current {@link ApplicationContext}
   *
   * @return a Hazelcast {@link Config}
   */
  @Singleton
  protected Config hazelcastConfig(SecurityAttributeLoader securityAttributeLoader,
      @Named(AssetCache.PORTFOLIO_CACHE_NAME) MapConfig portfolioMapConfig,
      @Named(AssetCache.POSITION_CACHE_NAME) MapConfig positionMapConfig,
      @Named(AssetCache.SECURITY_CACHE_NAME) MapConfig securityMapConfig,
      @Named(AssetCache.RULE_CACHE_NAME) MapConfig ruleMapConfig,
      SerializationConfig serializationConfig, ExecutorConfig clusterExecutorConfig,
      Optional<JetConfig> jetConfig, ApplicationContext applicationContext) {
    securityAttributeLoader.loadSecurityAttributes();

    boolean isClustered =
        (applicationContext.getEnvironment().getActiveNames().contains(Environment.KUBERNETES));

    Config config = new Config("panoptes");
    config.setClusterName("panoptes");
    config.setProperty("hazelcast.logging.type", "slf4j");
    config.setProperty("hazelcast.health.monitoring.threshold.memory.percentage", "90");
    config.setProperty("hazelcast.health.monitoring.threshold.cpu.percentage", "70");
    // this probably isn't good for fault tolerance but it improves startup time
    if (isClustered) {
      config.setProperty("hazelcast.initial.min.cluster.size", "3");
    }

    // use Hazelcast's ManagedContext mechanism to make the ApplicationContext available to
    // Hazelcast-instantiated objects (such as those that are deserialized by Hazelcast)
    ManagedContext applicationContextInjector = instance -> {
      if (instance instanceof ApplicationContextAware) {
        ((ApplicationContextAware) instance).setApplicationContext(applicationContext);
      }
      return instance;
    };
    config.setManagedContext(applicationContextInjector);

    config.setSerializationConfig(serializationConfig);

    // set up the entity caches (Portfolio, Position, etc.); note that Trades and Transactions
    // are non-persistent for now

    config.addMapConfig(portfolioMapConfig).addMapConfig(positionMapConfig)
        .addMapConfig(securityMapConfig).addMapConfig(ruleMapConfig)
        .addMapConfig(createMapConfiguration(AssetCache.TRADE_CACHE_NAME, null));

    config.addExecutorConfig(clusterExecutorConfig);

    // set up cluster join discovery appropriate for the detected environment
    if (isClustered) {
      // use Kubernetes discovery
      // TODO parameterize the cluster DNS property
      config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
      config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
          .setProperty("service-dns", "panoptes-hazelcast.default.svc.cluster.local");
    } else {
      // not running in Kubernetes; run standalone
      boolean isUseMulticast =
          (!applicationContext.getEnvironment().getActiveNames().contains(Environment.TEST));
      config.getNetworkConfig().getJoin().getAutoDetectionConfig().setEnabled(isUseMulticast);
      config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(isUseMulticast);
      config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(false);
    }

    jetConfig.ifPresent(config::setJetConfig);

    return config;
  }

  /**
   * Provides a {@link HazelcastInstance} configured with the given configuration.
   *
   * @param hazelcastConfiguration
   *     the Hazelcast {@link Config} with which to configure the instance
   * @param meterRegistry
   *     the {@link MeterRegistry} with which to register Hazelcast resources for monitoring
   *
   * @return a {@link HazelcastInstance}
   */
  @Bean(preDestroy = "shutdown")
  @Singleton
  protected HazelcastInstance hazelcastInstance(Config hazelcastConfiguration,
      MeterRegistry meterRegistry) {
    HazelcastInstance hazelcastInstance =
        Hazelcast.getOrCreateHazelcastInstance(hazelcastConfiguration);

    HazelcastCacheMetrics.monitor(meterRegistry,
        CacheBootstrap.getPortfolioCache(hazelcastInstance));
    HazelcastCacheMetrics.monitor(meterRegistry,
        CacheBootstrap.getPositionCache(hazelcastInstance));
    HazelcastCacheMetrics.monitor(meterRegistry, CacheBootstrap.getRuleCache(hazelcastInstance));
    HazelcastCacheMetrics.monitor(meterRegistry,
        CacheBootstrap.getSecurityCache(hazelcastInstance));
    HazelcastCacheMetrics.monitor(meterRegistry, CacheBootstrap.getTradeCache(hazelcastInstance));

    return hazelcastInstance;
  }

  /**
   * Provides a {@link MapConfig} for the {@link Portfolio} cache.
   *
   * @param mapStoreFactory
   *     the {@link HazelcastMapStoreFactory} to provide to the configuration; may be omitted if
   *     persistence is not desired (e.g. unit testing)
   *
   * @return a {@link MapConfig}
   */
  @Named(AssetCache.PORTFOLIO_CACHE_NAME)
  @Singleton
  protected MapConfig portfolioMapConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
    return createMapConfiguration(AssetCache.PORTFOLIO_CACHE_NAME, mapStoreFactory.orElse(null));
  }

  /**
   * Provides a {@link MapConfig} for the {@link Position} cache.
   *
   * @param mapStoreFactory
   *     the {@link HazelcastMapStoreFactory} to provide to the configuration; may be omitted if
   *     persistence is not desired (e.g. unit testing)
   *
   * @return a {@link MapConfig}
   */
  @Named(AssetCache.POSITION_CACHE_NAME)
  @Singleton
  protected MapConfig positionMapConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
    return createMapConfiguration(AssetCache.POSITION_CACHE_NAME, mapStoreFactory.orElse(null));
  }

  /**
   * Provides a {@link MapConfig} for the {@link Rule} cache.
   *
   * @param mapStoreFactory
   *     the {@link HazelcastMapStoreFactory} to provide to the configuration; may be omitted if
   *     persistence is not desired (e.g. unit testing)
   *
   * @return a {@link MapConfig}
   */
  @Named(AssetCache.RULE_CACHE_NAME)
  @Singleton
  protected MapConfig ruleMapConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
    return createMapConfiguration(AssetCache.RULE_CACHE_NAME, mapStoreFactory.orElse(null));
  }

  /**
   * Provides a {@link MapConfig} for the {@link Security} cache.
   *
   * @param mapStoreFactory
   *     the {@link HazelcastMapStoreFactory} to provide to the configuration; may be omitted if
   *     persistence is not desired (e.g. unit testing)
   *
   * @return a {@link MapConfig}
   */
  @Named(AssetCache.SECURITY_CACHE_NAME)
  @Singleton
  protected MapConfig securityMapConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
    return createMapConfiguration(AssetCache.SECURITY_CACHE_NAME, mapStoreFactory.orElse(null));
  }
}
