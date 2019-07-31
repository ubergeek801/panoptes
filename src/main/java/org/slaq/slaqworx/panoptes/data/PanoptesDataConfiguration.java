package org.slaq.slaqworx.panoptes.data;

import java.io.Serializable;

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
import com.hazelcast.config.NearCacheConfig;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

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
    public Config hazelcastConfig(ApplicationContext appContext) throws Exception {
        Config config = new Config();

        createMapConfiguration(config, PortfolioCache.PORTFOLIO_CACHE_NAME,
                new HibernateEntityMapStore<>(PortfolioCache.PORTFOLIO_CACHE_NAME,
                        appContext.getBean(PortfolioRepository.class)));
        createMapConfiguration(config, PortfolioCache.POSITION_CACHE_NAME,
                new HibernateEntityMapStore<>(PortfolioCache.POSITION_CACHE_NAME,
                        appContext.getBean(PositionRepository.class)));
        createMapConfiguration(config, PortfolioCache.SECURITY_CACHE_NAME,
                new HibernateEntityMapStore<>(PortfolioCache.SECURITY_CACHE_NAME,
                        appContext.getBean(SecurityRepository.class)));
        createMapConfiguration(config, PortfolioCache.RULE_CACHE_NAME,
                new HibernateEntityMapStore<>(PortfolioCache.RULE_CACHE_NAME,
                        appContext.getBean(RuleRepository.class)));

        if (System.getenv("KUBERNETES_SERVICE_HOST") == null) {
            // not running in Kubernetes; run standalone
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        } else {
            // use Kubernetes discovery
            // FIXME parameterize the cluster DNS property
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
     * @param loader
     *            the MapStore implementation to use for the map
     */
    protected void createMapConfiguration(Config config, String cacheName, Serializable loader) {
        MapStoreConfig mapStoreConfig = new MapStoreConfig().setImplementation(loader);
        NearCacheConfig nearCacheConfig =
                new NearCacheConfig().setInMemoryFormat(InMemoryFormat.BINARY);
        config.getMapConfig(cacheName).setBackupCount(0).setInMemoryFormat(InMemoryFormat.BINARY)
                .setMapStoreConfig(mapStoreConfig).setNearCacheConfig(nearCacheConfig);
    }
}
