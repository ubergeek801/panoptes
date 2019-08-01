package org.slaq.slaqworx.panoptes.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.hazelcast.core.IMap;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PimcoBenchmarkDatabaseLoader {
    private static final Logger LOG = LoggerFactory.getLogger(PimcoBenchmarkDatabaseLoader.class);

    @Autowired
    PortfolioCache portfolioCache;

    /**
     * Loads the cache (and then flushes to the database) using data from the PIMCO data source.
     */
    @Test
    public void loadDatabase() throws Exception {
        PimcoBenchmarkDataSource pimcoDataSource = PimcoBenchmarkDataSource.getInstance();

        IMap<SecurityKey, Security> securityMap =
                (IMap<SecurityKey, Security>)portfolioCache.getSecurityCache();
        LOG.info("adding {} securities to cache", pimcoDataSource.getSecurityMap().size());
        securityMap.putAll(pimcoDataSource.getSecurityMap());
        securityMap.flush();

        IMap<PortfolioKey, Portfolio> portfolioMap =
                (IMap<PortfolioKey, Portfolio>)portfolioCache.getPortfolioCache();
        IMap<PositionKey, Position> positionMap =
                (IMap<PositionKey, Position>)portfolioCache.getPositionCache();
        LOG.info("adding 4 benchmarks to cache");

        Portfolio benchmark = pimcoDataSource.getBenchmark(PimcoBenchmarkDataSource.EMAD_KEY);
        LOG.info("adding {} positions to cache", benchmark.getPositionSet().size());
        benchmark.getPositions().forEach(p -> positionMap.put(p.getKey(), p));
        portfolioMap.put(benchmark.getKey(), benchmark);

        benchmark = pimcoDataSource.getBenchmark(PimcoBenchmarkDataSource.GLAD_KEY);
        LOG.info("adding {} positions to cache", benchmark.getPositionSet().size());
        benchmark.getPositions().forEach(p -> positionMap.put(p.getKey(), p));
        portfolioMap.put(benchmark.getKey(), benchmark);

        benchmark = pimcoDataSource.getBenchmark(PimcoBenchmarkDataSource.ILAD_KEY);
        LOG.info("adding {} positions to cache", benchmark.getPositionSet().size());
        benchmark.getPositions().forEach(p -> positionMap.put(p.getKey(), p));
        portfolioMap.put(benchmark.getKey(), benchmark);

        benchmark = pimcoDataSource.getBenchmark(PimcoBenchmarkDataSource.PGOV_KEY);
        LOG.info("adding {} positions to cache", benchmark.getPositionSet().size());
        benchmark.getPositions().forEach(p -> positionMap.put(p.getKey(), p));
        portfolioMap.put(benchmark.getKey(), benchmark);

        portfolioMap.flush();
        positionMap.flush();

        LOG.info("completed database loading");
    }
}
