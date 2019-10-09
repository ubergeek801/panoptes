package org.slaq.slaqworx.panoptes.cache;

import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventPublisher;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteSystemProperties;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.store.CacheStore;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.ExecutorConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.DiscoveryEvent;
import org.apache.ignite.events.EventType;
import org.apache.ignite.internal.IgnitionEx;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.data.PortfolioCacheStore;
import org.slaq.slaqworx.panoptes.data.PositionCacheStore;
import org.slaq.slaqworx.panoptes.data.RuleCacheStore;
import org.slaq.slaqworx.panoptes.data.SecurityAttributeLoader;
import org.slaq.slaqworx.panoptes.data.SecurityCacheStore;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code PanoptesCacheConfiguration} is a Micronaut {@code Factory} that provides {@code Bean}s
 * related to the Ignite cache.
 *
 * @author jeremy
 */
@Factory
public class PanoptesCacheConfiguration {
    public static final String REMOTE_PORTFOLIO_EVALUATOR_EXECUTOR = "remote-portfolio-evaluator";

    private static final Logger LOG = LoggerFactory.getLogger(PanoptesCacheConfiguration.class);

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
     * @param cacheStoreFactory
     *            a {@code Factory} to be used to create a {@code CacheStore} for the cache, or
     *            {@code null} to use no {@code CacheStore}
     * @return a {@code CacheConfiguration} configured for the given cache
     */
    protected <K, V> CacheConfiguration<K, V> createCacheConfiguration(String cacheName,
            javax.cache.configuration.Factory<? extends CacheStore<? super K, ? super V>> cacheStoreFactory) {
        CacheConfiguration<K, V> cacheConfig =
                new CacheConfiguration<K, V>(cacheName).setCacheMode(CacheMode.REPLICATED);
        // these don't seem to affect (single-node) performance but they seem reasonable
        cacheConfig.setCopyOnRead(false).setEventsDisabled(true).setStoreByValue(false);
        if (cacheStoreFactory != null) {
            cacheConfig.setCacheStoreFactory(cacheStoreFactory);
        }

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
        // Ignite complains about having "different" CacheStore factories due to use of lambdas
        System.setProperty(IgniteSystemProperties.IGNITE_SKIP_CONFIGURATION_CONSISTENCY_CHECK,
                "true");

        IgniteConfiguration config = new IgniteConfiguration().setIgniteInstanceName("panoptes")
                .setGridLogger(new Slf4jLogger()).setMetricsLogFrequency(0);
        // parallelism is at the Rule level so set Portfolio-level concurrency conservatively
        config.setExecutorConfiguration(
                new ExecutorConfiguration(REMOTE_PORTFOLIO_EVALUATOR_EXECUTOR).setSize(1));

        // set up the entity caches (Portfolio, Position, etc.); note that Trade is non-persistent
        // for now

        CacheConfiguration<PortfolioKey, Portfolio> portfolioCacheConfiguration =
                createCacheConfiguration(AssetCache.PORTFOLIO_CACHE_NAME,
                        () -> new PortfolioCacheStore(AssetCache.PORTFOLIO_CACHE_NAME));
        CacheConfiguration<PositionKey, Position> positionCacheConfiguration =
                createCacheConfiguration(AssetCache.POSITION_CACHE_NAME,
                        () -> new PositionCacheStore(AssetCache.POSITION_CACHE_NAME));
        CacheConfiguration<RuleKey, ConfigurableRule> securityCacheConfiguration =
                createCacheConfiguration(AssetCache.RULE_CACHE_NAME,
                        () -> new RuleCacheStore(AssetCache.RULE_CACHE_NAME));
        CacheConfiguration<SecurityKey, Security> ruleCacheConfiguration =
                createCacheConfiguration(AssetCache.SECURITY_CACHE_NAME,
                        () -> new SecurityCacheStore(AssetCache.SECURITY_CACHE_NAME));
        config.setCacheConfiguration(portfolioCacheConfiguration, positionCacheConfiguration,
                securityCacheConfiguration, ruleCacheConfiguration,
                createCacheConfiguration(AssetCache.TRADE_CACHE_NAME, null));

        // set up cluster join discovery appropriate for the detected environment
        if (isClustered()) {
            // use Kubernetes discovery
            TcpDiscoveryKubernetesIpFinder k8sDiscovery = new TcpDiscoveryKubernetesIpFinder();
            k8sDiscovery.setServiceName("panoptes-ignite");
            config.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(k8sDiscovery));

            // delay cache loading until there are at least 4 members
            LOG.info("waiting for minimum of 4 cluster members");
            config.setIncludeEventTypes(EventType.EVT_NODE_JOINED)
                    .setLocalEventListeners(Map.of((IgnitePredicate<DiscoveryEvent>)event -> {
                        int clusterSize = event.topologyNodes().size();
                        if (clusterSize >= 4) {
                            // publishEvent() is synchronous so execute in a separate thread
                            new Thread(() -> eventPublisher.publishEvent(new ClusterReadyEvent()),
                                    "cluster-initializer").start();
                        } else {
                            LOG.info("cluster size now {} of 4 expected", clusterSize);
                        }

                        return true;
                    }, new int[] { EventType.EVT_NODE_JOINED }));
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

        if (!isClustered()) {
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
