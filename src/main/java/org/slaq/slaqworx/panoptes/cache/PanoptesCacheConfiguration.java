package org.slaq.slaqworx.panoptes.cache;

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
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to use to create the {@code MapStore}
     * @param cacheName
     *            the name of the map/cache being created
     * @return a {@code MapConfig} configured for the given map
     */
    protected MapConfig createMapConfiguration(HazelcastMapStoreFactory mapStoreFactory,
            String cacheName) {
        MapStoreConfig mapStoreConfig = new MapStoreConfig()
                .setFactoryImplementation(mapStoreFactory).setWriteDelaySeconds(15)
                .setWriteBatchSize(1000).setInitialLoadMode(InitialLoadMode.LAZY);
        NearCacheConfig nearCacheConfig = new NearCacheConfig().setName("near-" + cacheName)
                .setInMemoryFormat(InMemoryFormat.OBJECT).setCacheLocalEntries(true);
        MapConfig mapConfig = new MapConfig(cacheName).setBackupCount(3).setReadBackupData(true)
                .setInMemoryFormat(InMemoryFormat.BINARY).setMapStoreConfig(mapStoreConfig)
                .setNearCacheConfig(nearCacheConfig);

        return mapConfig;
    }

    /**
     * Provides a Hazelcast configuration suitable for the detected runtime environment.
     *
     * @param securityAttributeLoader
     *            the {@SecurityAttributeLoader} used to initialize {@code SecurityAttribute}s
     * @param hazelcastMapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to be used to create the
     *            {@code MapStoreConfig}s
     * @return a Hazelcast {@code Config}
     */
    @Bean
    protected Config hazelcastConfig(SecurityAttributeLoader securityAttributeLoader,
            HazelcastMapStoreFactory hazelcastMapStoreFactory,
            @Named("portfolio") MapConfig portfolioMapConfig,
            @Named("position") MapConfig positionMapConfig,
            @Named("security") MapConfig securityMapConfig,
            @Named("rule") MapConfig ruleMapConfig) {
        securityAttributeLoader.loadSecurityAttributes();

        boolean isClustered = (System.getenv("KUBERNETES_SERVICE_HOST") != null);

        Config config = new Config("panoptes");
        config.setProperty("hazelcast.logging.type", "slf4j");
        // this probably isn't good for fault tolerance but it improves startup time
        if (isClustered) {
            config.setProperty("hazelcast.initial.min.cluster.size", "4");
        }

        // set up the entity caches (Portfolio, Position, etc.)

        SerializationConfig serializationConfig = config.getSerializationConfig();
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(PortfolioKeySerializer.class).setTypeClass(PortfolioKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(PortfolioSerializer.class).setTypeClass(Portfolio.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(PositionKeySerializer.class).setTypeClass(PositionKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(PositionSerializer.class).setTypeClass(Position.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(RuleKeySerializer.class).setTypeClass(RuleKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(RuleSerializer.class).setTypeClass(ConfigurableRule.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(SecurityKeySerializer.class).setTypeClass(SecurityKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(SecuritySerializer.class).setTypeClass(Security.class));

        config.addMapConfig(portfolioMapConfig).addMapConfig(positionMapConfig)
                .addMapConfig(securityMapConfig).addMapConfig(ruleMapConfig);

        // set up a map to act as the portfolio evaluation result "topic"
        config.getMapConfig(PortfolioCache.PORTFOLIO_EVALUATION_RESULT_MAP_NAME).setBackupCount(0)
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
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration
     * @return a {@code MapConfig}
     */
    @Named("portfolio")
    @Bean
    protected MapConfig portfolioMapStoreConfig(HazelcastMapStoreFactory mapStoreFactory) {
        return createMapConfiguration(mapStoreFactory, PortfolioCache.PORTFOLIO_CACHE_NAME);
    }

    /**
     * Provides a {@code MapConfig} for the {@code Position} cache.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration
     * @return a {@code MapConfig}
     */
    @Named("position")
    @Bean
    protected MapConfig positionMapStoreConfig(HazelcastMapStoreFactory mapStoreFactory) {
        return createMapConfiguration(mapStoreFactory, PortfolioCache.POSITION_CACHE_NAME);
    }

    /**
     * Provides a {@code MapConfig} for the {@code Rule} cache.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration
     * @return a {@code MapConfig}
     */
    @Named("rule")
    @Bean
    protected MapConfig ruleMapStoreConfig(HazelcastMapStoreFactory mapStoreFactory) {
        return createMapConfiguration(mapStoreFactory, PortfolioCache.RULE_CACHE_NAME);
    }

    /**
     * Provides a {@code MapConfig} for the {@code Security} cache.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration
     * @return a {@code MapConfig}
     */
    @Named("security")
    @Bean
    protected MapConfig securityMapStoreConfig(HazelcastMapStoreFactory mapStoreFactory) {
        return createMapConfiguration(mapStoreFactory, PortfolioCache.SECURITY_CACHE_NAME);
    }
}
