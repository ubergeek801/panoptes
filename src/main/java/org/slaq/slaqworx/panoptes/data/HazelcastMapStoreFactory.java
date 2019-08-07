package org.slaq.slaqworx.panoptes.data;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStoreFactory;

import org.slaq.slaqworx.panoptes.Panoptes;

/**
 * <code>HazelcastMapStoreFactory</code> is a <code>MapStoreFactory</code> that provides
 * <code>MapStore</code>s for the cached <code>Map</code>s (e.g. <code>Portiofolio</code>,
 * <code>Position</code>, <code>Rule</code>, <code>Security</code>).
 *
 * @author jeremy
 */
public class HazelcastMapStoreFactory implements MapStoreFactory<Object, Object> {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public MapLoader newMapStore(String mapName, Properties properties) {
        ApplicationContext appContext = Panoptes.getApplicationContext();
        DataSource dataSource = appContext.getBean(DataSource.class);

        switch (mapName) {
        case PortfolioCache.PORTFOLIO_CACHE_NAME:
            return new PortfolioMapStore(appContext, dataSource);
        case PortfolioCache.POSITION_CACHE_NAME:
            return new PositionMapStore(dataSource);
        case PortfolioCache.RULE_CACHE_NAME:
            return new RuleMapStore(dataSource);
        case PortfolioCache.SECURITY_CACHE_NAME:
            return new SecurityMapStore(dataSource);
        }

        // TODO throw a better exception
        throw new RuntimeException("no MapStore defined for map " + mapName);
    }
}
