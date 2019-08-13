package org.slaq.slaqworx.panoptes.data;

import java.util.Properties;

import javax.inject.Singleton;
import javax.sql.DataSource;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStoreFactory;

import io.micronaut.context.ApplicationContext;

/**
 * {@code HazelcastMapStoreFactory} is a {@code MapStoreFactory} that provides {@code MapStore}s for
 * the cached {@code Map}s (e.g. {@code Portiofolio}, {@code Position}, {@code Rule},
 * {@code Security}).
 *
 * @author jeremy
 */
@Singleton
public class HazelcastMapStoreFactory implements MapStoreFactory<Object, Object> {
    private final ApplicationContext applicationContext;
    private final DataSource dataSource;

    /**
     * Creates a new {@code HazelcastMapStoreFactory}. Restricted because instances of this class
     * should be obtained from the {@code ApplicationContext}.
     *
     * @param applicationContext
     *            the {@code ApplicationContext} to use for {@code MapStore}s which require it
     * @param dataSource
     *            the {@code DataSource} to use for all {@code MapStore}s
     */
    protected HazelcastMapStoreFactory(ApplicationContext applicationContext,
            DataSource dataSource) {
        this.applicationContext = applicationContext;
        this.dataSource = dataSource;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public MapLoader newMapStore(String mapName, Properties properties) {
        switch (mapName) {
        case PortfolioCache.PORTFOLIO_CACHE_NAME:
            return new PortfolioMapStore(applicationContext, dataSource);
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
