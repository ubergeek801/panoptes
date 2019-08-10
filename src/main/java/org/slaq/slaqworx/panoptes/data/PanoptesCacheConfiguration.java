package org.slaq.slaqworx.panoptes.data;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.ManagedContext;
import com.hazelcast.spring.context.SpringManagedContext;

import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.ProxyFactory;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.rule.MaterializedRule;
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
 * {@code PanoptesCacheConfiguration} is a Spring {@code Configuration} that provides {@code Bean}s
 * related to the Hazelcast cache.
 *
 * @author jeremy
 */
@Configuration
public class PanoptesCacheConfiguration {
    /**
     * Creates a new {@code PanoptesCacheConfiguration}. Restricted because instances of this class
     * should be obtained through Spring (if it is needed at all).
     */
    protected PanoptesCacheConfiguration() {
        // nothing to do
    }

    /**
     * Provides a {@code ManagedContext} which enables {@code @SpringAware} annotation for objects
     * deserialized by Hazelcast.
     */
    @Bean
    public ManagedContext managedContext() {
        return new SpringManagedContext();
    }

    /**
     * Creates a {@code MapConfig} for the specified map and adds it to the given Hazelcast
     * {@code Config}.
     *
     * @param config
     *            the Hazelcast {@code Config} to which to add the {@code MapConfig}
     * @param cacheName
     *            the name of the map being configured
     */
    protected void createMapConfiguration(Config config, String cacheName) {
        MapStoreConfig mapStoreConfig =
                new MapStoreConfig().setFactoryClassName(HazelcastMapStoreFactory.class.getName())
                        .setWriteDelaySeconds(15).setWriteBatchSize(1000)
                        .setInitialLoadMode(InitialLoadMode.LAZY);
        NearCacheConfig nearCacheConfig = new NearCacheConfig().setName("near-" + cacheName)
                .setInMemoryFormat(InMemoryFormat.OBJECT).setCacheLocalEntries(true);
        config.getMapConfig(cacheName).setBackupCount(3).setReadBackupData(true)
                .setInMemoryFormat(InMemoryFormat.BINARY).setMapStoreConfig(mapStoreConfig)
                .setNearCacheConfig(nearCacheConfig);
    }

    /**
     * Provides a Hazelcast configuration suitable for the detected runtime environment. Spring Boot
     * will automatically use this configuration when it creates the {@code HazelcastInstance}.
     *
     * @return a Hazelcast {@code Config}
     */
    @Bean
    @DependsOn("securityAttributeLoader")
    protected Config hazelcastConfig() {
        Config config = new Config("panoptes");

        // allow Spring dependencies to be injected into deserialized objects
        config.setManagedContext(managedContext());

        // set up the entity caches (Portfolio, Position, etc.)

        SerializationConfig serializationConfig = config.getSerializationConfig();
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(PortfolioKeySerializer.class).setTypeClass(PortfolioKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(PortfolioSerializer.class).setTypeClass(Portfolio.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(PositionKeySerializer.class).setTypeClass(PositionKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(PositionSerializer.class).setTypeClass(MaterializedPosition.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(RuleKeySerializer.class).setTypeClass(RuleKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(RuleSerializer.class).setTypeClass(MaterializedRule.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(SecurityKeySerializer.class).setTypeClass(SecurityKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(SecuritySerializer.class).setTypeClass(Security.class));

        createMapConfiguration(config, PortfolioCache.PORTFOLIO_CACHE_NAME);
        createMapConfiguration(config, PortfolioCache.POSITION_CACHE_NAME);
        createMapConfiguration(config, PortfolioCache.SECURITY_CACHE_NAME);
        createMapConfiguration(config, PortfolioCache.RULE_CACHE_NAME);

        // set up the (local) queue for portfolio evaluation
        config.getQueueConfig(PortfolioCache.PORTFOLIO_EVALUATION_QUEUE_NAME).setBackupCount(0);

        // set up the map for portfolio evaluation processing input
        config.getMapConfig(PortfolioCache.PORTFOLIO_EVALUATION_REQUEST_MAP_NAME).setBackupCount(0)
                .setInMemoryFormat(InMemoryFormat.BINARY);

        // set up a map to act as the portfolio evaluation result "topic"
        config.getMapConfig(PortfolioCache.PORTFOLIO_EVALUATION_RESULT_MAP_NAME).setBackupCount(0)
                .setInMemoryFormat(InMemoryFormat.BINARY);

        // set up cluster join discovery appropriate for the detected environment
        if (System.getenv("KUBERNETES_SERVICE_HOST") == null) {
            // not running in Kubernetes; run standalone
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        } else {
            // use Kubernetes discovery
            // TODO parameterize the cluster DNS property
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                    .setProperty("service-dns", "panoptes-hazelcast.default.svc.cluster.local");
        }

        return config;
    }

    /**
     * Provides a {@code ProxyFactory} which uses the specified {@code ApplicationContext} to
     * resolve references.
     *
     * @param applicationContext
     *            the {@code ApplicationContext} to use when resolving references
     * @return a {@code ProxyFactory}
     */
    @Bean
    protected ProxyFactory proxyFactory(ApplicationContext applicationContext) {
        return new ProxyFactory(applicationContext);
    }
}
