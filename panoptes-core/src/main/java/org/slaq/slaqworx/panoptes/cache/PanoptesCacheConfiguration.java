package org.slaq.slaqworx.panoptes.cache;

import java.util.Optional;

import javax.inject.Named;
import javax.inject.Singleton;

import com.hazelcast.config.Config;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ManagedContext;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;

import org.slaq.slaqworks.panoptes.cache.Hazelcast4CacheMetrics;
import org.slaq.slaqworx.panoptes.data.HazelcastMapStoreFactory;
import org.slaq.slaqworx.panoptes.data.SecurityAttributeLoader;
import org.slaq.slaqworx.panoptes.util.ApplicationContextAware;

/**
 * {@code PanoptesCacheConfiguration} is a Micronaut {@code Factory} that provides {@code Bean}s
 * related to the Hazelcast cache.
 *
 * @author jeremy
 */
@Factory
public class PanoptesCacheConfiguration {
    /**
     * Creates a new {@code PanoptesCacheConfiguration}. Restricted because instances of this class
     * should be obtained through the {@code ApplicationContext} (if it is needed at all).
     */
    protected PanoptesCacheConfiguration() {
        // nothing to do
    }

    /**
     * Provides a {@code MapConfig} for the specified map and adds it to the given Hazelcast
     * {@code Config}.
     *
     * @param cacheName
     *            the name of the map/cache being created
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to use to create the {@code MapStore}, or
     *            {@code null} to use no {@code MapStore}
     * @return a {@code MapConfig} configured for the given map
     */
    protected MapConfig createMapConfiguration(String cacheName,
            HazelcastMapStoreFactory mapStoreFactory) {
        NearCacheConfig nearCacheConfig = new NearCacheConfig().setName("near-" + cacheName)
                .setInMemoryFormat(InMemoryFormat.OBJECT).setCacheLocalEntries(true);
        MapConfig mapConfig = new MapConfig(cacheName).setBackupCount(3).setReadBackupData(true)
                .setInMemoryFormat(InMemoryFormat.BINARY).setNearCacheConfig(nearCacheConfig);
        if (mapStoreFactory != null) {
            MapStoreConfig mapStoreConfig = new MapStoreConfig()
                    .setFactoryImplementation(mapStoreFactory).setWriteDelaySeconds(15)
                    .setWriteBatchSize(1000).setInitialLoadMode(InitialLoadMode.LAZY);
            mapConfig.setMapStoreConfig(mapStoreConfig);
        }

        return mapConfig;
    }

    /**
     * Provides a Hazelcast configuration suitable for the detected runtime environment.
     *
     * @param securityAttributeLoader
     *            the {@code SecurityAttributeLoader} used to initialize {@code SecurityAttribute}s
     * @param portfolioMapConfig
     *            the {@code MapConfig} to use for {@code Portfolio} data
     * @param positionMapConfig
     *            the {@code MapConfig} to use for {@code Position} data
     * @param securityMapConfig
     *            the {@code MapConfig} to use for {@code Security} data
     * @param ruleMapConfig
     *            the {@code MapConfig} to use for {@code Rule} data
     * @param serializationConfig
     *            the {@code SerializationConfig} to use for the cache
     * @param clusterExecutorConfig
     *            the {@code ExecutorConfig} to use for clustered execution
     * @param applicationContext
     *            the current {@code ApplicationContext}
     * @return a Hazelcast {@code Config}
     */
    @Singleton
    protected Config hazelcastConfig(SecurityAttributeLoader securityAttributeLoader,
            @Named(AssetCache.PORTFOLIO_CACHE_NAME) MapConfig portfolioMapConfig,
            @Named(AssetCache.POSITION_CACHE_NAME) MapConfig positionMapConfig,
            @Named(AssetCache.SECURITY_CACHE_NAME) MapConfig securityMapConfig,
            @Named(AssetCache.RULE_CACHE_NAME) MapConfig ruleMapConfig,
            SerializationConfig serializationConfig, ExecutorConfig clusterExecutorConfig,
            ApplicationContext applicationContext) {
        securityAttributeLoader.loadSecurityAttributes();

        boolean isClustered = (applicationContext.getEnvironment().getActiveNames()
                .contains(Environment.KUBERNETES));

        Config config = new Config("panoptes");
        config.setClusterName("panoptes");
        config.setProperty("hazelcast.logging.type", "slf4j");
        config.setProperty("hazelcast.health.monitoring.threshold.memory.percentage", "90");
        config.setProperty("hazelcast.health.monitoring.threshold.cpu.percentage", "70");
        // this probably isn't good for fault tolerance but it improves startup time
        if (isClustered) {
            config.setProperty("hazelcast.initial.min.cluster.size", "4");
        }

        // use Hazelcast's ManagedContext mechanism to make the ApplicationContext available to
        // Hazelcast-instantiated objects (such as those that are deserialized by Hazelcast)
        ManagedContext applicationContextInjector = instance -> {
            if (instance instanceof ApplicationContextAware) {
                ((ApplicationContextAware)instance).setApplicationContext(applicationContext);
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
            boolean isUseMulticast = (!applicationContext.getEnvironment().getActiveNames()
                    .contains(Environment.TEST));
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(isUseMulticast);
            config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(false);
        }

        return config;
    }

    /**
     * Provides a {@code HazelcastInstance} configured with the given configuration.
     *
     * @param hazelcastConfiguration
     *            the Hazelcast {@Config} with which to configure the instance
     * @param meterRegistry
     *            the {@code MeterRegistry} with which to register Hazelcast resources for
     *            monitoring
     * @return a {@code HazelcastInstance}
     */
    @Bean(preDestroy = "shutdown")
    @Singleton
    protected HazelcastInstance hazelcastInstance(Config hazelcastConfiguration,
            MeterRegistry meterRegistry) {
        HazelcastInstance hazelcastInstance =
                Hazelcast.getOrCreateHazelcastInstance(hazelcastConfiguration);

        Hazelcast4CacheMetrics.monitor(meterRegistry,
                CacheBootstrap.getPortfolioCache(hazelcastInstance));
        Hazelcast4CacheMetrics.monitor(meterRegistry,
                CacheBootstrap.getPositionCache(hazelcastInstance));
        Hazelcast4CacheMetrics.monitor(meterRegistry,
                CacheBootstrap.getRuleCache(hazelcastInstance));
        Hazelcast4CacheMetrics.monitor(meterRegistry,
                CacheBootstrap.getSecurityCache(hazelcastInstance));
        Hazelcast4CacheMetrics.monitor(meterRegistry,
                CacheBootstrap.getTradeCache(hazelcastInstance));

        return hazelcastInstance;
    }

    /**
     * Provides a {@code MapConfig} for the {@code Portfolio} cache.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration; may be
     *            omitted if persistence is not desired (e.g. unit testing)
     * @return a {@code MapConfig}
     */
    @Named(AssetCache.PORTFOLIO_CACHE_NAME)
    @Singleton
    protected MapConfig portfolioMapConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
        return createMapConfiguration(AssetCache.PORTFOLIO_CACHE_NAME,
                mapStoreFactory.orElse(null));
    }

    /**
     * Provides a {@code MapConfig} for the {@code Position} cache.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration; may be
     *            omitted if persistence is not desired (e.g. unit testing)
     * @return a {@code MapConfig}
     */
    @Named(AssetCache.POSITION_CACHE_NAME)
    @Singleton
    protected MapConfig positionMapConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
        return createMapConfiguration(AssetCache.POSITION_CACHE_NAME, mapStoreFactory.orElse(null));
    }

    /**
     * Provides a {@code MapConfig} for the {@code Rule} cache.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration; may be
     *            omitted if persistence is not desired (e.g. unit testing)
     * @return a {@code MapConfig}
     */
    @Named(AssetCache.RULE_CACHE_NAME)
    @Singleton
    protected MapConfig ruleMapConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
        return createMapConfiguration(AssetCache.RULE_CACHE_NAME, mapStoreFactory.orElse(null));
    }

    /**
     * Provides a {@code MapConfig} for the {@code Security} cache.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration; may be
     *            omitted if persistence is not desired (e.g. unit testing)
     * @return a {@code MapConfig}
     */
    @Named(AssetCache.SECURITY_CACHE_NAME)
    @Singleton
    protected MapConfig securityMapConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
        return createMapConfiguration(AssetCache.SECURITY_CACHE_NAME, mapStoreFactory.orElse(null));
    }
}
