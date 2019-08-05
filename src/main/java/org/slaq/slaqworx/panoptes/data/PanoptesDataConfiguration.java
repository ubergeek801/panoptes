package org.slaq.slaqworx.panoptes.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;

import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.ProxyFactory;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
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
 * PanoptesDataConfiguration is a Spring Configuration that provides Beans related to DataSources,
 * EntityManagers, etc.
 *
 * @author jeremy
 */
@Configuration
public class PanoptesDataConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(PanoptesDataConfiguration.class);

    /**
     * Creates a new PanoptesDataConfiguration. Restricted because instances of this class should be
     * obtained through Spring (if it is needed at all).
     */
    protected PanoptesDataConfiguration() {
        // nothing to do
    }

    /**
     * Provides a Hazelcast configuration suitable for the detected runtime environment. Spring Boot
     * will automatically use this configuration when it creates the HazelcastInstance.
     *
     * @return a Hazelcast Config
     * @throws Exception
     *             if the configuration could not be created
     */
    @Bean
    @DependsOn("securityAttributeLoader")
    public Config hazelcastConfig() {
        Config config = new Config();

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
     * A pseudo-Bean that initializes the known SecurityAttributes. Dependent services such as the
     * Hazelcast cache may @DependsOn this.
     *
     * @param jdbcTemplate
     *            a JdbcTemplate from which to obtain data
     * @return a meaningless Void
     */
    @Bean
    public Void securityAttributeLoader(JdbcTemplate jdbcTemplate) {
        LOG.info("loading {} SecurityAttributes", jdbcTemplate
                .queryForObject("select count(*) from security_attribute", Integer.class));
        jdbcTemplate.query("select name, index, type from security_attribute",
                (RowCallbackHandler)(rs -> {
                    String name = rs.getString(1);
                    int index = rs.getInt(2);
                    String className = rs.getString(3);
                    Class<?> clazz;
                    try {
                        clazz = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        clazz = String.class;
                        LOG.warn("cannot locate class {} for SecurityAttribute {}", className,
                                name);
                    }
                    SecurityAttribute.of(name, index, clazz);
                }));

        return null;
    }

    /**
     * Creates a MapConfig for the specified map and adds it to the given Hazelcast Config.
     *
     * @param config
     *            the Hazelcast Config to which to add the MapConfig
     * @param cacheName
     *            the name of the map being configured
     */
    protected void createMapConfiguration(Config config, String cacheName) {
        MapStoreConfig mapStoreConfig =
                new MapStoreConfig().setFactoryClassName(HazelcastMapStoreFactory.class.getName())
                        .setWriteDelaySeconds(15).setWriteBatchSize(1000)
                        .setInitialLoadMode(InitialLoadMode.LAZY);
        NearCacheConfig nearCacheConfig =
                new NearCacheConfig().setInMemoryFormat(InMemoryFormat.BINARY);
        config.getMapConfig(cacheName).setBackupCount(0).setInMemoryFormat(InMemoryFormat.BINARY)
                .setMapStoreConfig(mapStoreConfig).setNearCacheConfig(nearCacheConfig);
    }

    /**
     * Provides a ProxyFactory which uses the specified AppliactionContext to resolve references.
     *
     * @param applicationContext
     *            the ApplicationContext to use when resolving references
     * @return a ProxyFactory
     */
    @Bean
    protected ProxyFactory proxyFactory(ApplicationContext applicationContext) {
        return new ProxyFactory(applicationContext);
    }
}
