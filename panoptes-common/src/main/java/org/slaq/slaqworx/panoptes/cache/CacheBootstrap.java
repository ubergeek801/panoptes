package org.slaq.slaqworx.panoptes.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
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
   * Obtains the {@code Portfolio} cache from the given {@code HazelcastInstance}.
   *
   * @param hazelcastInstance
   *     the {@code HazelcastInstance} from which to obtain the cache
   *
   * @return the {@code Portfolio} cache
   */
  protected static IMap<PortfolioKey, Portfolio>
  getPortfolioCache(HazelcastInstance hazelcastInstance) {
    return hazelcastInstance.getMap(AssetCache.PORTFOLIO_CACHE_NAME);
  }

  /**
   * Obtains the {@code Position} cache from the given {@code HazelcastInstance}.
   *
   * @param hazelcastInstance
   *     the {@code HazelcastInstance} from which to obtain the cache
   *
   * @return the {@code Position} cache
   */
  protected static IMap<PositionKey, Position>
  getPositionCache(HazelcastInstance hazelcastInstance) {
    return hazelcastInstance.getMap(AssetCache.POSITION_CACHE_NAME);
  }

  /**
   * Obtains the {@code Rule} cache from the given {@code HazelcastInstance}.
   *
   * @param hazelcastInstance
   *     the {@code HazelcastInstance} from which to obtain the cache
   *
   * @return the {@code Rule} cache
   */
  protected static IMap<RuleKey, ConfigurableRule>
  getRuleCache(HazelcastInstance hazelcastInstance) {
    return hazelcastInstance.getMap(AssetCache.RULE_CACHE_NAME);
  }

  /**
   * Obtains the {@code Security} cache from the given {@code HazelcastInstance}.
   *
   * @param hazelcastInstance
   *     the {@code HazelcastInstance} from which to obtain the cache
   *
   * @return the {@code Security} cache
   */
  protected static IMap<SecurityKey, Security>
  getSecurityCache(HazelcastInstance hazelcastInstance) {
    return hazelcastInstance.getMap(AssetCache.SECURITY_CACHE_NAME);
  }

  /**
   * Obtains the {@code Trade} cache from the given {@code HazelcastInstance}.
   *
   * @param hazelcastInstance
   *     the {@code HazelcastInstance} from which to obtain the cache
   *
   * @return the {@code Trade} cache
   */
  protected static IMap<TradeKey, Trade> getTradeCache(HazelcastInstance hazelcastInstance) {
    return hazelcastInstance.getMap(AssetCache.TRADE_CACHE_NAME);
  }

  /**
   * Creates a new {@code CacheBootstrap}. Restricted to enforce class utility semantics.
   */
  private CacheBootstrap() {
    // nothing to do
  }
}
