package org.slaq.slaqworx.panoptes.cache;

import java.util.List;

import javax.inject.Singleton;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteSystemProperties;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.ExecutorConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.EventType;
import org.apache.ignite.internal.IgnitionEx;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventPublisher;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.data.SecurityAttributeLoader;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * {@code PanoptesCacheConfiguration} is a Micronaut {@code Factory} that provides {@code Bean}s
 * related to the Ignite cache.
 *
 * @author jeremy
 */
@Factory
public class PanoptesCacheConfiguration {
    public static final String REMOTE_PORTFOLIO_EVALUATOR_EXECUTOR = "remote-portfolio-evaluator";

    /**
     * Creates a new {@code PanoptesCacheConfiguration}. Restricted because instances of this class
     * should be obtained through the {@code ApplicationContext} (if it is needed at all).
     */
    protected PanoptesCacheConfiguration() {
        // nothing to do
    }

    /**
     * Provides a {@code CacheConfiguration} for the specified cache.
     *
     * @param <K>
     *            the cache key type
     * @param <V>
     *            the cache value type
     * @param cacheName
     *            the name of the cache being created
     * @return a {@code CacheConfiguration} configured for the given cache
     */
    protected <K, V> CacheConfiguration<K, V> createCacheConfiguration(String cacheName) {
        CacheConfiguration<K, V> cacheConfig =
                new CacheConfiguration<K, V>(cacheName).setCacheMode(CacheMode.REPLICATED);
        // minor (or not) performance tweaks
        cacheConfig.setEventsDisabled(true).setStoreByValue(false);

        return cacheConfig;
    }

    /**
     * Provides an Ignite configuration suitable for the detected runtime environment.
     *
     * @param securityAttributeLoader
     *            the {@code SecurityAttributeLoader} used to initialize {@code SecurityAttribute}s
     * @param eventPublisher
     *            an {@code ApplicationEventPublisher} to be used to publish interesting cluster
     *            events
     * @return an {@code IgniteConfiguration}
     */
    @Singleton
    protected IgniteConfiguration igniteConfig(SecurityAttributeLoader securityAttributeLoader,
            ApplicationEventPublisher eventPublisher) {
        // ensure that Security attributes are loaded before caching fun begins
        securityAttributeLoader.loadSecurityAttributes();

        // ironically, setting quiet mode=false eliminates duplicate logging output
        System.setProperty(IgniteSystemProperties.IGNITE_QUIET, "false");
        // Ignite recommends this when using IPv4 in a mixed environment
        System.setProperty("java.net.preferIPv4Stack", "true");

        IgniteConfiguration config = new IgniteConfiguration().setIgniteInstanceName("panoptes")
                .setGridLogger(new Slf4jLogger()).setMetricsLogFrequency(0)
                .setDataStorageConfiguration(
                        new DataStorageConfiguration().setDefaultDataRegionConfiguration(
                                new DataRegionConfiguration().setInitialSize(1536L * 1024 * 1024)
                                        .setMaxSize(1536L * 1024 * 1024)));
        // parallelism is at the Rule level so set Portfolio-level concurrency conservatively
        config.setExecutorConfiguration(
                new ExecutorConfiguration(REMOTE_PORTFOLIO_EVALUATOR_EXECUTOR).setSize(1));

        // set up the entity caches (Portfolio, Position, etc.)
        CacheConfiguration<PortfolioKey, Portfolio> portfolioCacheConfiguration =
                createCacheConfiguration(AssetCache.PORTFOLIO_CACHE_NAME);
        CacheConfiguration<PositionKey, Position> positionCacheConfiguration =
                createCacheConfiguration(AssetCache.POSITION_CACHE_NAME);
        CacheConfiguration<RuleKey, ConfigurableRule> securityCacheConfiguration =
                createCacheConfiguration(AssetCache.RULE_CACHE_NAME);
        CacheConfiguration<SecurityKey, Security> ruleCacheConfiguration =
                createCacheConfiguration(AssetCache.SECURITY_CACHE_NAME);
        CacheConfiguration<TradeKey, Trade> tradeCacheConfiguration =
                createCacheConfiguration(AssetCache.TRADE_CACHE_NAME);
        config.setCacheConfiguration(portfolioCacheConfiguration, positionCacheConfiguration,
                securityCacheConfiguration, ruleCacheConfiguration, tradeCacheConfiguration);

        // set up cluster join discovery appropriate for the detected environment
        if (isClustered()) {
            // use Kubernetes discovery
            TcpDiscoveryKubernetesIpFinder k8sDiscovery = new TcpDiscoveryKubernetesIpFinder();
            k8sDiscovery.setServiceName("panoptes-ignite");
            config.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(k8sDiscovery));

            config.setIncludeEventTypes(EventType.EVT_NODE_JOINED);
        } else {
            // not running in Kubernetes; run standalone
            config.setDiscoverySpi(new TcpDiscoverySpi()
                    .setIpFinder(new TcpDiscoveryVmIpFinder().setAddresses(List.of("127.0.0.1"))));
        }

        return config;
    }

    /**
     * Provides an {@code Ignite} instance configured with the given configuration.
     *
     * @param igniteConfiguration
     *            the {@code IgniteConfiguration} with which to configure the instance
     * @param eventPublisher
     *            an {@code ApplicationEventPublisher} to be used to publish interesting cluster
     *            events
     * @return an {@Ignite} instance
     * @throws IgniteCheckedException
     *             if the {code Ignite} instance could not be created
     */
    @Singleton
    protected Ignite igniteInstance(IgniteConfiguration igniteConfiguration,
            ApplicationContext applicationContext, ApplicationEventPublisher eventPublisher)
            throws IgniteCheckedException {
        Ignite igniteInstance = IgnitionEx.start(igniteConfiguration,
                new GridMicronautResourceContext(applicationContext), false).get1();
        if (isClustered()) {
            // the cluster initializer will wait for a suitable cluster topology
            igniteInstance.services().deployClusterSingleton("cluster-initializer",
                    new ClusterInitializer());
        } else {
            // we are good to go
            eventPublisher.publishEvent(new ClusterReadyEvent());
        }

        return igniteInstance;
    }

    /**
     * Indicates whether the runtime environment is part of a cluster.
     *
     * @return {@code true} if a clustered environment is detected, {@code false} otherwise
     */
    protected boolean isClustered() {
        return (System.getenv("KUBERNETES_SERVICE_HOST") != null);
    }
}
