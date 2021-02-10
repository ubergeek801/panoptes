package org.slaq.slaqworx.panoptes.data;

import java.util.Properties;

import javax.inject.Singleton;

import com.hazelcast.map.MapLoader;
import com.hazelcast.map.MapStoreFactory;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;

import org.slaq.slaqworx.panoptes.cache.AssetCache;

/**
 * A {@code MapStoreFactory} that provides {@code MapStore}s for the cached {@code Map}s (e.g.
 * {@code Portfolio}, {@code Position}, {@code Rule}, {@code Security}).
 *
 * @author jeremy
 */
@Singleton
@Requires(notEnv = { Environment.TEST, "offline" })
public class HazelcastMapStoreFactory implements MapStoreFactory<Object, Object> {
    private final PortfolioMapStore portfolioMapStore;
    private final PositionMapStore positionMapStore;
    private final RuleMapStore ruleMapStore;
    private final SecurityMapStore securityMapStore;

    /**
     * Creates a new {@code HazelcastMapStoreFactory}. Restricted because instances of this class
     * should be obtained from the {@code ApplicationContext}.
     *
     * @param portfolioMapStore
     *            the {@code MapStore} to use for the {@code Portfolio} cache
     * @param positionMapStore
     *            the {@code MapStore} to use for the {@code Position} cache
     * @param ruleMapStore
     *            the {@code MapStore} to use for the {@code Rule} cache
     * @param securityMapStore
     *            the {@code MapStore} to use for the {@code Security} cache
     */
    protected HazelcastMapStoreFactory(PortfolioMapStore portfolioMapStore,
            PositionMapStore positionMapStore, RuleMapStore ruleMapStore,
            SecurityMapStore securityMapStore) {
        this.portfolioMapStore = portfolioMapStore;
        this.positionMapStore = positionMapStore;
        this.ruleMapStore = ruleMapStore;
        this.securityMapStore = securityMapStore;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
