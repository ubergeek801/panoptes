package org.slaq.slaqworx.panoptes.data;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.hazelcast.core.IMap;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * PimcoBenchmarkDatabaseLoader populates the Hazelcast caches (and thus the database implementing
 * their MapStores) with data based on the PIMCO benchmarks.
 *
 * @author jeremy
 */
@SpringBootApplication
public class PimcoBenchmarkDatabaseLoader {
    private static final Logger LOG = LoggerFactory.getLogger(PimcoBenchmarkDatabaseLoader.class);

    public static void main(String[] args) {
        SpringApplication.run(PimcoBenchmarkDatabaseLoader.class, args);
    }

    /**
     * Creates a new PimcoBenchmarkDatabaseLoader.
     */
    public PimcoBenchmarkDatabaseLoader() {
        // nothing to do
    }

    /**
     * Loads the cache (and then flushes to the database) using data from the PIMCO data source.
     *
     * @param appContext
     *            the Spring ApplicationContext
     * @return an ApplicationRunner which performs the cache load
     */
    @Bean
    public ApplicationRunner startupRunner(ApplicationContext appContext) {
        return args -> {
            PortfolioCache portfolioCache = appContext.getBean(PortfolioCache.class);
            PimcoBenchmarkDataSource pimcoDataSource = PimcoBenchmarkDataSource.getInstance();

            IMap<SecurityKey, Security> securityMap = portfolioCache.getSecurityCache();
            LOG.info("adding {} securities to cache", pimcoDataSource.getSecurityMap().size());
            securityMap.putAll(pimcoDataSource.getSecurityMap());
            securityMap.flush();

            DummyPortfolioMapLoader mapLoader = new DummyPortfolioMapLoader();
            ArrayList<Portfolio> portfolios = new ArrayList<>();
            for (PortfolioKey key : mapLoader.loadAllKeys()) {
                Portfolio portfolio = mapLoader.load(key);
                portfolios.add(portfolio);
            }

            IMap<RuleKey, Rule> ruleMap = portfolioCache.getRuleCache();
            portfolios.stream().forEach(pf -> {
                LOG.info("persisting {} Rules for Portfolio {}", pf.getRuleKeys().count(),
                        pf.getName());
                pf.getRules(mapLoader).forEach(r -> ruleMap.put(r.getKey(), r));
            });
            ruleMap.flush();

            IMap<PositionKey, Position> positionMap = portfolioCache.getPositionCache();
            portfolios.stream().forEach(pf -> {
                LOG.info("persisting {} Positions for Portfolio {}", pf.getPositionSet().size(),
                        pf.getName());
                pf.getPositions().forEach(p -> positionMap.put(p.getKey(), p));
            });
            positionMap.flush();

            IMap<PortfolioKey, Portfolio> portfolioMap = portfolioCache.getPortfolioCache();
            LOG.info("persisting {} Portfolios", portfolios.size());
            portfolios.stream().forEach(pf -> portfolioMap.put(pf.getKey(), pf));
            portfolioMap.flush();

            LOG.info("completed database loading");
        };
    }
}
