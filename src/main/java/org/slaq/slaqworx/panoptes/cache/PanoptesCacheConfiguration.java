package org.slaq.slaqworx.panoptes.cache;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Singleton;

import io.micronaut.context.annotation.Factory;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.store.CacheStore;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.ExecutorConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.IgniteSpiContext;
import org.apache.ignite.spi.IgniteSpiException;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;

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
        if (cacheStoreFactory != null) {
            cacheConfig.setCacheStoreFactory(cacheStoreFactory);
        }

        return cacheConfig;
    }

    /**
     * Provides an Ignite configuration suitable for the detected runtime environment.
     *
     * @param securityAttributeLoader
     *            the {@SecurityAttributeLoader} used to initialize {@code SecurityAttribute}s
     * @return an {@code IgniteConfiguration}
     */
    @Singleton
    protected IgniteConfiguration igniteConfig(SecurityAttributeLoader securityAttributeLoader) {
        securityAttributeLoader.loadSecurityAttributes();

        boolean isClustered = (System.getenv("KUBERNETES_SERVICE_HOST") != null);

        IgniteConfiguration config = new IgniteConfiguration().setIgniteInstanceName("panoptes")
                .setGridLogger(new Slf4jLogger()).setMetricsLogFrequency(0);
        // parallelism is at the Rule level so set Portfolio-level concurrency conservatively
        config.setExecutorConfiguration(
                new ExecutorConfiguration(REMOTE_PORTFOLIO_EVALUATOR_EXECUTOR).setSize(1));

        // set up the entity caches (Portfolio, Position, etc.); note that Trade is non-persistent
        // for now

        CacheConfiguration<PortfolioKey, Portfolio> portfolioCacheConfiguration =
                createCacheConfiguration(AssetCache.PORTFOLIO_CACHE_NAME,
                        () -> new PortfolioCacheStore());
        CacheConfiguration<PositionKey, Position> positionCacheConfiguration =
                createCacheConfiguration(AssetCache.POSITION_CACHE_NAME,
                        () -> new PositionCacheStore());
        CacheConfiguration<RuleKey, ConfigurableRule> securityCacheConfiguration =
                createCacheConfiguration(AssetCache.RULE_CACHE_NAME, () -> new RuleCacheStore());
        CacheConfiguration<SecurityKey, Security> ruleCacheConfiguration = createCacheConfiguration(
                AssetCache.SECURITY_CACHE_NAME, () -> new SecurityCacheStore());
        config.setCacheConfiguration(portfolioCacheConfiguration, positionCacheConfiguration,
                securityCacheConfiguration, ruleCacheConfiguration,
                createCacheConfiguration(AssetCache.TRADE_CACHE_NAME, null));

        // set up cluster join discovery appropriate for the detected environment
        if (isClustered) {
            // use Kubernetes discovery
            // FIXME configure k8s discovery
            // TODO parameterize the cluster DNS property
            config.setDiscoverySpi(
                    new TcpDiscoverySpi().setIpFinder(new TcpDiscoveryKubernetesIpFinder()));
        } else {
            // not running in Kubernetes; run standalone (or as close as we can get, by using a
            // dummy discovery)
            config.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(new TcpDiscoveryIpFinder() {
                @Override
                public void close() {
                    // nothing to do
                }

                @Override
                public Collection<InetSocketAddress> getRegisteredAddresses()
                        throws IgniteSpiException {
                    return Collections.emptyList();
                }

                @Override
                public void initializeLocalAddresses(Collection<InetSocketAddress> addrs)
                        throws IgniteSpiException {
                    // nothing to do
                }

                @Override
                public boolean isShared() {
                    return true;
                }

                @Override
                public void onSpiContextDestroyed() {
                    // nothing to do
                }

                @Override
                public void onSpiContextInitialized(IgniteSpiContext spiCtx) {
                    // nothing to do
                }

                @Override
                public void registerAddresses(Collection<InetSocketAddress> addrs) {
                    // nothing to do
                }

                @Override
                public void unregisterAddresses(Collection<InetSocketAddress> addrs) {
                    // nothing to do
                }
            }));
        }

        return config;
    }

    /**
     * Provides an {@code Ignite} instance configured with the given configuration.
     *
     * @param igniteConfiguration
     *            the {@code IgniteConfiguration} with which to configure the instance
     * @return an {@Ignite} instance
     */
    @Singleton
    protected Ignite igniteInstance(IgniteConfiguration igniteConfiguration) {
        return Ignition.getOrStart(igniteConfiguration);
    }
}
