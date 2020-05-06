package org.slaq.slaqworx.panoptes.cache;

import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import javax.inject.Singleton;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.PositionProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.trade.TradeProvider;
import org.slaq.slaqworx.panoptes.util.DistinctSecurityAttributeValuesAggregator;
import org.slaq.slaqworx.panoptes.util.ForkJoinPoolFactory;

/**
 * {@code AssetCache} provides operations for accessing {@code Portfolio} and related data (e.g.
 * {@code Position}s, {@code Security} entities) from the distributed cache.
 *
 * @author jeremy
 */
@Singleton
public class AssetCache implements PortfolioProvider, PositionProvider, RuleProvider,
        SecurityProvider, TradeProvider {
    public static final String PORTFOLIO_CACHE_NAME = "portfolio";
    public static final String POSITION_CACHE_NAME = "position";
    public static final String SECURITY_CACHE_NAME = "security";
    public static final String RULE_CACHE_NAME = "rule";
    public static final String TRADE_CACHE_NAME = "trade";

    protected static final String CLUSTER_EXECUTOR_NAME = "cluster-executor";

    private static final ForkJoinPool localExecutorThreadPool = ForkJoinPoolFactory
            .newForkJoinPool(ForkJoinPool.getCommonPoolParallelism(), "local-executor");

    /**
     * Obtains the {@code ExecutorService} used to perform local computations (typically
     * {@code Portfolio} and {@code Trade} evaluations). Callers may use this to submit processing
     * requests in parallel, which will generally result in better performance than using a separate
     * {@code ExecutorService}.
     *
     * @return an {@code ExecutorService} used to evaluate {@code Trade}s
     */
    public static ExecutorService getLocalExecutor() {
        return localExecutorThreadPool;
    }

    private final HazelcastInstance hazelcastInstance;
    private final IMap<PortfolioKey, Portfolio> portfolioCache;
    private final IMap<PositionKey, Position> positionCache;
    private final IMap<RuleKey, ConfigurableRule> ruleCache;
    private final IMap<SecurityKey, Security> securityCache;
    private final IMap<TradeKey, Trade> tradeCache;

    /**
     * Creates a new {@code AssetCache}. Restricted because instances of this class should be
     * obtained through the {@code ApplicationContext}.
     *
     * @param hazelcastInstance
     *            the {@code HazelcastInstance} through which to access cached data
     */
    protected AssetCache(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        portfolioCache = CacheBootstrap.getPortfolioCache(hazelcastInstance);
        positionCache = CacheBootstrap.getPositionCache(hazelcastInstance);
        ruleCache = CacheBootstrap.getRuleCache(hazelcastInstance);
        securityCache = CacheBootstrap.getSecurityCache(hazelcastInstance);
        tradeCache = CacheBootstrap.getTradeCache(hazelcastInstance);
    }

    /**
     * Obtains an {@code IExecutorService} suitable for performing computations (such as portfolio
     * compliance evaluations) on the cluster.
     *
     * @return an {@code IExecutorService}
     */
    public IExecutorService getClusterExecutor() {
        return hazelcastInstance.getExecutorService(CLUSTER_EXECUTOR_NAME);
    }

    /**
     * Obtains the set of distinct countries used by current {@code Security} entities.
     *
     * @return a set of country names
     */
    public SortedSet<String> getCountries() {
        return getSecurityCache().aggregate(
                new DistinctSecurityAttributeValuesAggregator<>(SecurityAttribute.country));
    }

    /**
     * Obtains the set of distinct currencies used by current {@code Security} entities.
     *
     * @return a set of currency symbols
     */
    public SortedSet<String> getCurrencies() {
        return getSecurityCache().aggregate(
                new DistinctSecurityAttributeValuesAggregator<>(SecurityAttribute.currency));
    }

    @Override
    public Portfolio getPortfolio(PortfolioKey key) {
        return getPortfolioCache().get(key);
    }

    /**
     * Obtains the {@code Portfolio} cache.
     *
     * @return the {@code Portfolio} cache
     */
    public IMap<PortfolioKey, Portfolio> getPortfolioCache() {
        return portfolioCache;
    }

    @Override
    public Position getPosition(PositionKey key) {
        return getPositionCache().get(key);
    }

    /**
     * Obtains the {@code Position} cache.
     *
     * @return the {@code Position} cache
     */
    public IMap<PositionKey, Position> getPositionCache() {
        return positionCache;
    }

    /**
     * Obtains the set of distinct regions used by current {@code Security} entities.
     *
     * @return a set of region names
     */
    public SortedSet<String> getRegions() {
        return getSecurityCache().aggregate(
                new DistinctSecurityAttributeValuesAggregator<>(SecurityAttribute.region));
    }

    @Override
    public ConfigurableRule getRule(RuleKey key) {
        return getRuleCache().get(key);
    }

    /**
     * Obtains the {@code Rule} cache.
     *
     * @return the {@code Rule} cache
     */
    public IMap<RuleKey, ConfigurableRule> getRuleCache() {
        return ruleCache;
    }

    /**
     * Obtains the set of distinct sectors used by current {@code Security} entities.
     *
     * @return a set of sector names
     */
    public SortedSet<String> getSectors() {
        return getSecurityCache().aggregate(
                new DistinctSecurityAttributeValuesAggregator<>(SecurityAttribute.sector));
    }

    @Override
    public Security getSecurity(SecurityKey key, EvaluationContext evaluationContext) {
        return getSecurityCache().get(key);
    }

    /**
     * Obtains the {@code Security} cache.
     *
     * @return the {@code Security} cache
     */
    public IMap<SecurityKey, Security> getSecurityCache() {
        return securityCache;
    }

    @Override
    public Trade getTrade(TradeKey key) {
        return getTradeCache().get(key);
    }

    /**
     * Obtains the {@code Trade} cache.
     *
     * @return the {@code Trade} cache
     */
    public IMap<TradeKey, Trade> getTradeCache() {
        return tradeCache;
    }
}
