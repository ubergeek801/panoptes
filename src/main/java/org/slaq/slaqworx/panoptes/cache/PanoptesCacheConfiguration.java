package org.slaq.slaqworx.panoptes.cache;

import java.util.Optional;

import javax.inject.Named;

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
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.serializer.PortfolioKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioSerializer;
import org.slaq.slaqworx.panoptes.serializer.PositionKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.PositionSerializer;
import org.slaq.slaqworx.panoptes.serializer.RuleKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.RuleSerializer;
import org.slaq.slaqworx.panoptes.serializer.SecurityKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.SecuritySerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeSerializer;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;

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
     *            null to use no {@code MapStore}
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
     *            the {@SecurityAttributeLoader} used to initialize {@code SecurityAttribute}s
     * @param portfolioMapConfig
     *            the {@code MapConfig} to use for {@code Portfolio} data
     * @param positionMapConfig
     *            the {@code MapConfig} to use for {@code Position} data
     * @param securityMapConfig
     *            the {@code MapConfig} to use for {@code Security} data
     * @param ruleMapConfig
     *            the {@code MapConfig} to use for {@code Rule} data
     * @param portfolioSerializer
     *            the {@code PortfolioSerializer} to use for {@code Portfolio} serialization
     * @param positionSerializer
     *            the {@code PositionSerializer} to use for {@code Position} serialization
     * @param tradeSerializer
     *            the {@code TradeSerializer} to use for {@code Trade} serialization
     * @return a Hazelcast {@code Config}
     */
    @Bean
    protected Config hazelcastConfig(SecurityAttributeLoader securityAttributeLoader,
            @Named("portfolio") MapConfig portfolioMapConfig,
            @Named("position") MapConfig positionMapConfig,
            @Named("security") MapConfig securityMapConfig, @Named("rule") MapConfig ruleMapConfig,
            PortfolioSerializer portfolioSerializer, PositionSerializer positionSerializer,
            TradeSerializer tradeSerializer) {
        securityAttributeLoader.loadSecurityAttributes();

        boolean isClustered = (System.getenv("KUBERNETES_SERVICE_HOST") != null);

        Config config = new Config("panoptes");
        config.setProperty("hazelcast.logging.type", "slf4j");
        // this probably isn't good for fault tolerance but it improves startup time
        if (isClustered) {
            config.setProperty("hazelcast.initial.min.cluster.size", "4");
        }

        // set up the entity caches (Portfolio, Position, etc.); note that Trade is non-persistent
        // for now

        SerializationConfig serializationConfig = config.getSerializationConfig();
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new PortfolioKeySerializer()).setTypeClass(PortfolioKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(portfolioSerializer).setTypeClass(Portfolio.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new PositionKeySerializer()).setTypeClass(PositionKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(positionSerializer).setTypeClass(Position.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new RuleKeySerializer()).setTypeClass(RuleKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new RuleSerializer()).setTypeClass(ConfigurableRule.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new SecurityKeySerializer()).setTypeClass(SecurityKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new SecuritySerializer()).setTypeClass(Security.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new TradeKeySerializer()).setTypeClass(TradeKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(tradeSerializer).setTypeClass(Trade.class));

        config.addMapConfig(portfolioMapConfig).addMapConfig(positionMapConfig)
                .addMapConfig(securityMapConfig).addMapConfig(ruleMapConfig)
                .addMapConfig(createMapConfiguration(AssetCache.TRADE_CACHE_NAME, null));

        // set up a map to act as the portfolio evaluation result "topic"
        config.getMapConfig(AssetCache.PORTFOLIO_EVALUATION_RESULT_MAP_NAME).setBackupCount(0)
                .setInMemoryFormat(InMemoryFormat.BINARY);

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
     * @return a {@HazelcastInstance}
     */
    @Bean
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
    @Bean
    protected MapConfig
            portfolioMapStoreConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
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
    @Bean
    protected MapConfig positionMapStoreConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
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
    @Bean
    protected MapConfig ruleMapStoreConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
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
    @Bean
    protected MapConfig securityMapStoreConfig(Optional<HazelcastMapStoreFactory> mapStoreFactory) {
        return createMapConfiguration(AssetCache.SECURITY_CACHE_NAME, mapStoreFactory.orElse(null));
    }
}
