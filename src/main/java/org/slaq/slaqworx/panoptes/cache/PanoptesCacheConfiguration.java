package org.slaq.slaqworx.panoptes.cache;

import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ManagedContext;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.data.HazelcastMapStoreFactory;
import org.slaq.slaqworx.panoptes.data.SecurityAttributeLoader;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.serializer.EvaluationContextSerializer;
import org.slaq.slaqworx.panoptes.serializer.EvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioSerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioSummarySerializer;
import org.slaq.slaqworx.panoptes.serializer.PositionKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.PositionSerializer;
import org.slaq.slaqworx.panoptes.serializer.RuleKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.RuleSerializer;
import org.slaq.slaqworx.panoptes.serializer.SecurityKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.SecuritySerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeEvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeSerializer;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.ui.PortfolioSummary;
import org.slaq.slaqworx.panoptes.util.ApplicationContextAware;

/**
 * {@code PanoptesCacheConfiguration} is a Micronaut {@code Factory} that provides {@code Bean}s
 * related to the Hazelcast cache.
 *
 * @author jeremy
 */
@Factory
public class PanoptesCacheConfiguration {
    protected static final String REMOTE_PORTFOLIO_EVALUATOR_EXECUTOR = "cluster-executor";

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
     * @param assetCacheProvider
     *            a {@code Provider} providing an {@code AssetCache} (used to avoid circular
     *            injection dependencies)
     * @param applicationContext
     *            the current {@code ApplicationContext}
     * @return a Hazelcast {@code Config}
     */
    @Singleton
    protected Config hazelcastConfig(SecurityAttributeLoader securityAttributeLoader,
            @Named("portfolio") MapConfig portfolioMapConfig,
            @Named("position") MapConfig positionMapConfig,
            @Named("security") MapConfig securityMapConfig, @Named("rule") MapConfig ruleMapConfig,
            Provider<AssetCache> assetCacheProvider, ApplicationContext applicationContext) {
        securityAttributeLoader.loadSecurityAttributes();

        boolean isClustered = (System.getenv("KUBERNETES_SERVICE_HOST") != null);

        Config config = new Config("panoptes");
        config.setProperty("hazelcast.logging.type", "slf4j");
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

        SerializationConfig serializationConfig = config.getSerializationConfig();
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new EvaluationContextSerializer(assetCacheProvider))
                .setTypeClass(EvaluationContext.class));
        serializationConfig.addSerializerConfig(
                new SerializerConfig().setImplementation(new EvaluationResultSerializer())
                        .setTypeClass(EvaluationResult.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new PortfolioEvaluationRequestSerializer(assetCacheProvider))
                .setTypeClass(PortfolioEvaluationRequest.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new PortfolioKeySerializer()).setTypeClass(PortfolioKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new PortfolioSerializer(assetCacheProvider))
                .setTypeClass(Portfolio.class));
        serializationConfig.addSerializerConfig(
                new SerializerConfig().setImplementation(new PortfolioSummarySerializer())
                        .setTypeClass(PortfolioSummary.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new PositionKeySerializer()).setTypeClass(PositionKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new PositionSerializer()).setTypeClass(Position.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new RuleKeySerializer()).setTypeClass(RuleKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new RuleSerializer()).setTypeClass(ConfigurableRule.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new SecurityKeySerializer()).setTypeClass(SecurityKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new SecuritySerializer()).setTypeClass(Security.class));
        serializationConfig.addSerializerConfig(
                new SerializerConfig().setImplementation(new TradeEvaluationResultSerializer())
                        .setTypeClass(TradeEvaluationResult.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new TradeKeySerializer()).setTypeClass(TradeKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new TradeSerializer()).setTypeClass(Trade.class));

        // set up the entity caches (Portfolio, Position, etc.); note that Trades and Transactions
        // are non-persistent for now

        config.addMapConfig(portfolioMapConfig).addMapConfig(positionMapConfig)
                .addMapConfig(securityMapConfig).addMapConfig(ruleMapConfig)
                .addMapConfig(createMapConfiguration(AssetCache.TRADE_CACHE_NAME, null));

        // set up the Portfolio evaluator executor
        config.getExecutorConfig(REMOTE_PORTFOLIO_EVALUATOR_EXECUTOR)
                .setPoolSize(ForkJoinPool.getCommonPoolParallelism())
                .setName(REMOTE_PORTFOLIO_EVALUATOR_EXECUTOR);

        // set up cluster join discovery appropriate for the detected environment
        if (isClustered) {
            // use Kubernetes discovery
            // TODO parameterize the cluster DNS property
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                    .setProperty("service-dns", "panoptes-hazelcast.default.svc.cluster.local");
        } else {
            // not running in Kubernetes; run standalone
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        }

        return config;
    }

    /**
     * Provides a {@code HazelcastInstance} configured with the given configuration.
     *
     * @param hazelcastConfiguration
     *            the Hazelcast {@Config} with which to configure the instance
     * @return a {@code HazelcastInstance}
     */
    @Bean(preDestroy = "shutdown")
    @Singleton
    protected HazelcastInstance hazelcastInstance(Config hazelcastConfiguration) {
        return Hazelcast.newHazelcastInstance(hazelcastConfiguration);
    }

    /**
     * Provides a {@code MapConfig} for the {@code Portfolio} cache.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration; may be
     *            omitted if persistence is not desired (e.g. unit testing)
     * @return a {@code MapConfig}
     */
    @Named("portfolio")
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
    @Named("position")
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
    @Named("rule")
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
    @Named("security")
    @Singleton
    protected MapConfig securityMapConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
        return createMapConfiguration(AssetCache.SECURITY_CACHE_NAME, mapStoreFactory.orElse(null));
    }
}
