package org.slaq.slaqworx.panoptes.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import java.util.Set;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * A class utility for "bootstrapping" cache resources, to avoid circular initialization
 * dependencies.
 *
 * @author jeremy
 */
class CacheBootstrap {
  /**
   * Creates a new {@link CacheBootstrap}. Restricted to enforce class utility semantics.
   */
  private CacheBootstrap() {
    // nothing to do
  }

  /**
   * Obtains the eligibility cache from the given {@link HazelcastInstance}.
   *
   * @param hazelcastInstance
   *     the {@link HazelcastInstance} from which to obtain the cache
   *
   * @return the eligibility cache
   */
  @Nonnull
  protected static IMap<String, Set<String>> getEligibilityCache(
      @Nonnull HazelcastInstance hazelcastInstance) {
    return hazelcastInstance.getMap(AssetCache.ELIGIBILITY_CACHE_NAME);
  }

  /**
   * Obtains the {@link Portfolio} cache from the given {@link HazelcastInstance}.
   *
   * @param hazelcastInstance
   *     the {@link HazelcastInstance} from which to obtain the cache
   *
   * @return the {@link Portfolio} cache
   */
  @Nonnull
  protected static IMap<PortfolioKey, Portfolio> getPortfolioCache(
      @Nonnull HazelcastInstance hazelcastInstance) {
    return hazelcastInstance.getMap(AssetCache.PORTFOLIO_CACHE_NAME);
  }

  /**
   * Obtains the {@link Position} cache from the given {@link HazelcastInstance}.
   *
   * @param hazelcastInstance
   *     the {@link HazelcastInstance} from which to obtain the cache
   *
   * @return the {@link Position} cache
   */
  @Nonnull
  protected static IMap<PositionKey, Position> getPositionCache(
      @Nonnull HazelcastInstance hazelcastInstance) {
    return hazelcastInstance.getMap(AssetCache.POSITION_CACHE_NAME);
  }

  /**
   * Obtains the {@link Rule} cache from the given {@link HazelcastInstance}.
   *
   * @param hazelcastInstance
   *     the {@link HazelcastInstance} from which to obtain the cache
   *
   * @return the {@link Rule} cache
   */
  @Nonnull
  protected static IMap<RuleKey, ConfigurableRule> getRuleCache(
      @Nonnull HazelcastInstance hazelcastInstance) {
    return hazelcastInstance.getMap(AssetCache.RULE_CACHE_NAME);
  }

  /**
   * Obtains the {@link Security} cache from the given {@link HazelcastInstance}.
   *
   * @param hazelcastInstance
   *     the {@link HazelcastInstance} from which to obtain the cache
   *
   * @return the {@link Security} cache
   */
  @Nonnull
  protected static IMap<SecurityKey, Security> getSecurityCache(
      @Nonnull HazelcastInstance hazelcastInstance) {
    return hazelcastInstance.getMap(AssetCache.SECURITY_CACHE_NAME);
  }

  /**
   * Obtains the {@link Trade} cache from the given {@link HazelcastInstance}.
   *
   * @param hazelcastInstance
   *     the {@link HazelcastInstance} from which to obtain the cache
   *
   * @return the {@link Trade} cache
   */
  @Nonnull
  protected static IMap<TradeKey, Trade> getTradeCache(
      @Nonnull HazelcastInstance hazelcastInstance) {
    return hazelcastInstance.getMap(AssetCache.TRADE_CACHE_NAME);
  }
}
