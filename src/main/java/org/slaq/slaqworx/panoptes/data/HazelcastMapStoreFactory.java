package org.slaq.slaqworx.panoptes.data;

import java.util.Properties;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.sql.DataSource;

import com.hazelcast.map.MapLoader;
import com.hazelcast.map.MapStoreFactory;

import io.micronaut.context.annotation.Requires;

import org.slaq.slaqworx.panoptes.cache.AssetCache;

/**
 * {@code HazelcastMapStoreFactory} is a {@code MapStoreFactory} that provides {@code MapStore}s for
 * the cached {@code Map}s (e.g. {@code Portiofolio}, {@code Position}, {@code Rule},
 * {@code Security}).
 *
 * @author jeremy
 */
@Singleton
@Requires(notEnv = { "test", "offline" })
public class HazelcastMapStoreFactory implements MapStoreFactory<Object, Object> {
    private final Provider<AssetCache> assetCacheProvider;
    private final DataSource dataSource;

    /**
     * Creates a new {@code HazelcastMapStoreFactory}. Restricted because instances of this class
     * should be obtained from the {@code ApplicationContext}.
     *
     * @param assetCacheProvider
     *            the {@code AsssetCache} from which to obtained cached data, wrapped in a
     *            {@code Provider} to avoid a circular injection dependency
     * @param dataSource
     *            the {@code DataSource} to use for all {@code MapStore}s
     */
    protected HazelcastMapStoreFactory(Provider<AssetCache> assetCacheProvider,
            DataSource dataSource) {
        this.assetCacheProvider = assetCacheProvider;
        this.dataSource = dataSource;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public MapLoader newMapStore(String mapName, Properties properties) {
        switch (mapName) {
        case AssetCache.PORTFOLIO_CACHE_NAME:
            return new PortfolioMapStore(assetCacheProvider, dataSource);
        case AssetCache.POSITION_CACHE_NAME:
            return new PositionMapStore(dataSource);
        case AssetCache.RULE_CACHE_NAME:
            return new RuleMapStore(dataSource);
        case AssetCache.SECURITY_CACHE_NAME:
            return new SecurityMapStore(dataSource);
        }

        // TODO throw a better exception
        throw new RuntimeException("no MapStore defined for map " + mapName);
    }
}
