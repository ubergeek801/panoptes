package org.slaq.slaqworx.panoptes.data;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.offline.DummyPortfolioMapLoader;
import org.slaq.slaqworx.panoptes.offline.PimcoBenchmarkDataSource;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code PimcoBenchmarkDatabaseLoader} populates the Panoptes database (using Hazelcast
 * {@code MapStore}s) with data based on the PIMCO benchmarks.
 *
 * @author jeremy
 */
@Singleton
@Requires(notEnv = Environment.TEST)
public class PimcoBenchmarkDatabaseLoader {
    private static final Logger LOG = LoggerFactory.getLogger(PimcoBenchmarkDatabaseLoader.class);

    public static void main(String[] args) {
        Micronaut.run(PimcoBenchmarkDatabaseLoader.class, args);
    }

    /**
     * Creates a new {@code PimcoBenchmarkDatabaseLoader}.
     */
    public PimcoBenchmarkDatabaseLoader() {
        // nothing to do
    }

    /**
     * Loads the cache (and then flushes to the database) using data from the PIMCO data source.
     *
     * @param event
     *            a {@code StartupEvent}
     * @throws Exception
     *             if an unexpected error occurs
     */
    @EventListener()
    protected void onStartup(StartupEvent event) throws Exception {
        @SuppressWarnings("resource") BeanContext beanContext = event.getSource();

        PimcoBenchmarkDataSource pimcoDataSource = PimcoBenchmarkDataSource.getInstance();
        TransactionTemplate txTemplate = beanContext.getBean(TransactionTemplate.class);
        HazelcastMapStoreFactory mapStoreFactory =
                beanContext.getBean(HazelcastMapStoreFactory.class);

        LOG.info("persisting {} Securities", pimcoDataSource.getSecurityMap().size());
        SecurityMapStore securityMapStore =
                (SecurityMapStore)mapStoreFactory.newMapStore(AssetCache.SECURITY_CACHE_NAME, null);

        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                securityMapStore.storeAll(pimcoDataSource.getSecurityMap());
            }
        });

        DummyPortfolioMapLoader mapLoader = new DummyPortfolioMapLoader();
        ArrayList<Portfolio> portfolios = new ArrayList<>();
        for (PortfolioKey key : mapLoader.loadAllKeys()) {
            Portfolio portfolio = mapLoader.load(key);
            portfolios.add(portfolio);
        }

        RuleMapStore ruleMapStore =
                (RuleMapStore)mapStoreFactory.newMapStore(AssetCache.RULE_CACHE_NAME, null);
        portfolios.stream().forEach(pf -> {
            LOG.info("persisting {} Rules for Portfolio \"{}\"", pf.getRules().count(),
                    pf.getName());
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Map<RuleKey, ConfigurableRule> ruleMap = pf.getRules()
                            .collect(Collectors.toMap(Rule::getKey, r -> (ConfigurableRule)r));
                    ruleMapStore.storeAll(ruleMap);
                }
            });
        });

        PositionMapStore positionMapStore =
                (PositionMapStore)mapStoreFactory.newMapStore(AssetCache.POSITION_CACHE_NAME, null);
        portfolios.stream().forEach(pf -> {
            LOG.info("persisting {} Positions for Portfolio \"{}\"", pf.getPositions().count(),
                    pf.getName());
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Map<PositionKey, Position> positionMap =
                            pf.getPositions().collect(Collectors.toMap(Position::getKey, p -> p));
                    positionMapStore.storeAll(positionMap);
                }
            });
        });

        PortfolioMapStore portfolioMapStore = (PortfolioMapStore)mapStoreFactory
                .newMapStore(AssetCache.PORTFOLIO_CACHE_NAME, null);
        // persist the benchmarks first
        LOG.info("persisting 4 benchmark Portfolios");
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                portfolioMapStore.store(PimcoBenchmarkDataSource.EMAD_KEY,
                        pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.EMAD_KEY));
                portfolioMapStore.store(PimcoBenchmarkDataSource.GLAD_KEY,
                        pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.GLAD_KEY));
                portfolioMapStore.store(PimcoBenchmarkDataSource.ILAD_KEY,
                        pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.ILAD_KEY));
                portfolioMapStore.store(PimcoBenchmarkDataSource.PGOV_KEY,
                        pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.PGOV_KEY));
            }
        });
        portfolios.stream().filter(p -> p.getKey().getId().length() != 4).forEach(p -> {
            LOG.info("persisting Portfolio {}", p.getKey());
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    portfolioMapStore.store(p.getKey(), p);
                }
            });
        });

        LOG.info("completed database loading");
    }
}
