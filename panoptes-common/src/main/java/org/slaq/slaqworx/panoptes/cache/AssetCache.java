package org.slaq.slaqworx.panoptes.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Singleton;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import javax.annotation.Nonnull;
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
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.trade.TradeProvider;
import org.slaq.slaqworx.panoptes.util.DistinctSecurityAttributeValuesAggregator;
import org.slaq.slaqworx.panoptes.util.ForkJoinPoolFactory;

/**
 * Provides operations for accessing {@link Portfolio} and related data (e.g. {@link Position}s,
 * {@link Security} entities) from the distributed cache.
 *
 * @author jeremy
 */
@Singleton
public class AssetCache
    implements PortfolioProvider, PositionProvider, RuleProvider, SecurityProvider, TradeProvider {
  public static final String ELIGIBILITY_CACHE_NAME = "eligibility";
  public static final String PORTFOLIO_CACHE_NAME = "portfolio";
  public static final String POSITION_CACHE_NAME = "position";
  public static final String SECURITY_CACHE_NAME = "security";
  public static final String RULE_CACHE_NAME = "rule";
  public static final String TRADE_CACHE_NAME = "trade";

  protected static final String CLUSTER_EXECUTOR_NAME = "cluster-executor";

  private static final ForkJoinPool localExecutorThreadPool =
      ForkJoinPoolFactory.newForkJoinPool(ForkJoinPool.getCommonPoolParallelism(),
          "local-executor");

  private static AssetCache defaultAssetCache;
  private final HazelcastInstance hazelcastInstance;
  private final IMap<String, Set<String>> eligibilityCache;
  private final IMap<PortfolioKey, Portfolio> portfolioCache;
  private final IMap<PositionKey, Position> positionCache;
  private final IMap<RuleKey, ConfigurableRule> ruleCache;
  private final IMap<SecurityKey, Security> securityCache;
  private final IMap<TradeKey, Trade> tradeCache;

  /**
   * Creates a new {@link AssetCache}. Restricted because instances of this class should be obtained
   * through the {@link ApplicationContext}.
   *
   * @param hazelcastInstance
   *     the {@link HazelcastInstance} through which to access cached data
   */
  protected AssetCache(HazelcastInstance hazelcastInstance) {
    this.hazelcastInstance = hazelcastInstance;
    eligibilityCache = CacheBootstrap.getEligibilityCache(hazelcastInstance);
    portfolioCache = CacheBootstrap.getPortfolioCache(hazelcastInstance);
    positionCache = CacheBootstrap.getPositionCache(hazelcastInstance);
    ruleCache = CacheBootstrap.getRuleCache(hazelcastInstance);
    securityCache = CacheBootstrap.getSecurityCache(hazelcastInstance);
    tradeCache = CacheBootstrap.getTradeCache(hazelcastInstance);

    defaultAssetCache = this;
  }

  /**
   * Obtains the default {@link AssetCache} instance, to be used "in case of emergency."
   *
   * @return the default {@link AssetCache} instance
   */
  @Nonnull
  public static AssetCache getDefault() {
    return defaultAssetCache;
  }

  /**
   * Sets the default {@link AssetCache} instance, to be used "in case of emergency."
   *
   * @param assetCache
   *     the default {@link AssetCache} instance
   */
  public static void setDefault(@Nonnull AssetCache assetCache) {
    defaultAssetCache = assetCache;
  }

  /**
   * Obtains the {@link ExecutorService} used to perform local computations (typically {@link
   * Portfolio} and {@link Trade} evaluations). Callers may use this to submit processing requests
   * in parallel, which will generally result in better performance than using a separate {@link
   * ExecutorService}.
   *
   * @return an {@link ExecutorService} used to evaluate {@link Trade}s
   */
  @Nonnull
  public static ExecutorService getLocalExecutor() {
    return localExecutorThreadPool;
  }

  /**
   * Obtains an {@link IExecutorService} suitable for performing computations (such as portfolio
   * compliance evaluations) on the cluster.
   *
   * @return an {@link IExecutorService}
   */
  @Nonnull
  public IExecutorService getClusterExecutor() {
    return hazelcastInstance.getExecutorService(CLUSTER_EXECUTOR_NAME);
  }

  /**
   * Obtains the set of distinct countries used by current {@link Security} entities.
   *
   * @return a set of country names
   */
  @Nonnull
  public SortedSet<String> getCountries() {
    return getSecurityCache().aggregate(
        new DistinctSecurityAttributeValuesAggregator<>(SecurityAttribute.country));
  }

  /**
   * Obtains the set of distinct currencies used by current {@link Security} entities.
   *
   * @return a set of currency symbols
   */
  @Nonnull
  public SortedSet<String> getCurrencies() {
    return getSecurityCache().aggregate(
        new DistinctSecurityAttributeValuesAggregator<>(SecurityAttribute.currency));
  }

  /**
   * Obtains the eligibility cache.
   *
   * @return the eligibility cache
   */
  @Nonnull
  public IMap<String, Set<String>> getEligibilityCache() {
    return eligibilityCache;
  }

  @Override
  public Portfolio getPortfolio(@Nonnull PortfolioKey key) {
    return getPortfolioCache().get(key);
  }

  /**
   * Obtains the {@link Portfolio} cache.
   *
   * @return the {@link Portfolio} cache
   */
  @Nonnull
  public IMap<PortfolioKey, Portfolio> getPortfolioCache() {
    return portfolioCache;
  }

  @Override
  public Position getPosition(@Nonnull PositionKey key) {
    return getPositionCache().get(key);
  }

  /**
   * Obtains the {@link Position} cache.
   *
   * @return the {@link Position} cache
   */
  @Nonnull
  public IMap<PositionKey, Position> getPositionCache() {
    return positionCache;
  }

  /**
   * Obtains the set of distinct regions used by current {@link Security} entities.
   *
   * @return a set of region names
   */
  @Nonnull
  public SortedSet<String> getRegions() {
    return getSecurityCache().aggregate(
        new DistinctSecurityAttributeValuesAggregator<>(SecurityAttribute.region));
  }

  @Override
  public ConfigurableRule getRule(@Nonnull RuleKey key) {
    return getRuleCache().get(key);
  }

  /**
   * Obtains the {@link Rule} cache.
   *
   * @return the {@link Rule} cache
   */
  @Nonnull
  public IMap<RuleKey, ConfigurableRule> getRuleCache() {
    return ruleCache;
  }

  /**
   * Obtains the set of distinct sectors used by current {@link Security} entities.
   *
   * @return a set of sector names
   */
  @Nonnull
  public SortedSet<String> getSectors() {
    return getSecurityCache().aggregate(
        new DistinctSecurityAttributeValuesAggregator<>(SecurityAttribute.sector));
  }

  @Override
  public Security getSecurity(@Nonnull SecurityKey key,
      @Nonnull EvaluationContext evaluationContext) {
    return getSecurityCache().get(key);
  }

  /**
   * Obtains the {@link Security} cache.
   *
   * @return the {@link Security} cache
   */
  @Nonnull
  public IMap<SecurityKey, Security> getSecurityCache() {
    return securityCache;
  }

  @Override
  public Trade getTrade(TradeKey key) {
    return getTradeCache().get(key);
  }

  /**
   * Obtains the {@link Trade} cache.
   *
   * @return the {@link Trade} cache
   */
  @Nonnull
  public IMap<TradeKey, Trade> getTradeCache() {
    return tradeCache;
  }
}
