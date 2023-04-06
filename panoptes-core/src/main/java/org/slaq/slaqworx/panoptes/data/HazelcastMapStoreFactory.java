package org.slaq.slaqworx.panoptes.data;

import com.hazelcast.map.IMap;
import com.hazelcast.map.MapLoader;
import com.hazelcast.map.MapStore;
import com.hazelcast.map.MapStoreFactory;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;
import java.util.Properties;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * A {@link MapStoreFactory} that provides {@link MapStore}s for the cached {@link IMap}s (e.g.
 * {@link Portfolio}, {@link Position}, {@link Rule}, {@link Security}).
 *
 * @author jeremy
 */
@Singleton
@Requires(notEnv = {Environment.TEST, "offline"})
public class HazelcastMapStoreFactory implements MapStoreFactory<Object, Object> {
  private final PortfolioMapStore portfolioMapStore;
  private final PositionMapStore positionMapStore;
  private final RuleMapStore ruleMapStore;
  private final SecurityMapStore securityMapStore;

  /**
   * Creates a new {@link HazelcastMapStoreFactory}. Restricted because instances of this class
   * should be obtained from the {@link ApplicationContext}.
   *
   * @param portfolioMapStore the {@link MapStore} to use for the {@link Portfolio} cache
   * @param positionMapStore the {@link MapStore} to use for the {@link Position} cache
   * @param ruleMapStore the {@link MapStore} to use for the {@link Rule} cache
   * @param securityMapStore the {@link MapStore} to use for the {@link Security} cache
   */
  protected HazelcastMapStoreFactory(
      PortfolioMapStore portfolioMapStore,
      PositionMapStore positionMapStore,
      RuleMapStore ruleMapStore,
      SecurityMapStore securityMapStore) {
    this.portfolioMapStore = portfolioMapStore;
    this.positionMapStore = positionMapStore;
    this.ruleMapStore = ruleMapStore;
    this.securityMapStore = securityMapStore;
  }

  @Override
  public MapLoader newMapStore(String mapName, Properties properties) {
    switch (mapName) {
      case AssetCache.PORTFOLIO_CACHE_NAME:
        return portfolioMapStore;
      case AssetCache.POSITION_CACHE_NAME:
        return positionMapStore;
      case AssetCache.RULE_CACHE_NAME:
        return ruleMapStore;
      case AssetCache.SECURITY_CACHE_NAME:
        return securityMapStore;
    }

    // TODO throw a better exception
    throw new RuntimeException("no MapStore defined for map " + mapName);
  }
}
