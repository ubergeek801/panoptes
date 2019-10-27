package org.slaq.slaqworx.panoptes.data;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code PimcoBenchmarkDatabaseLoader} populates the Panoptes database (using
 * {@code IgniteCacheStore}s) with data based on the PIMCO benchmarks.
 *
 * @author jeremy
 */
@Singleton
@Requires(notEnv = "test")
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
     * Loads the cache (and then flushes to the database) using data from the PIMCO data source. We
     * slightly abuse the {@code CacheWriter}s in doing so (by using them to store data that was
     * never in the cache in the first place).
     *
     * @param event
     *            a {@code StartupEvent}
     */
    @EventListener()
    protected void onStartup(StartupEvent event) throws Exception {
        @SuppressWarnings("resource")
        BeanContext beanContext = event.getSource();

        PimcoBenchmarkDataSource pimcoDataSource = PimcoBenchmarkDataSource.getInstance();
        TransactionTemplate txTemplate = beanContext.getBean(TransactionTemplate.class);

        LOG.info("persisting {} Securities", pimcoDataSource.getSecurityMap().size());
        SecurityCacheStore securityCacheStore = beanContext.getBean(SecurityCacheStore.class);

        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                securityCacheStore.writeAll(pimcoDataSource.getSecurityMap());
            }
        });

        DummyPortfolioCacheLoader mapLoader = new DummyPortfolioCacheLoader();
        ArrayList<Portfolio> portfolios = new ArrayList<>();
        mapLoader.inputIterator().forEachRemaining(key -> {
            Portfolio portfolio = mapLoader.load(key);
            portfolios.add(portfolio);
        });

        RuleCacheStore ruleCacheStore = beanContext.getBean(RuleCacheStore.class);
        portfolios.stream().forEach(pf -> {
            LOG.info("persisting {} Rules for Portfolio \"{}\"", pf.getRules().count(),
                    pf.getName());
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Map<RuleKey, ConfigurableRule> ruleMap = pf.getRules()
                            .collect(Collectors.toMap(r -> r.getKey(), r -> (ConfigurableRule)r));
                    ruleCacheStore.writeAll(ruleMap);
                }
            });
        });

        PositionCacheStore positionCacheStore = beanContext.getBean(PositionCacheStore.class);
        portfolios.stream().forEach(pf -> {
            LOG.info("persisting {} Positions for Portfolio \"{}\"", pf.getPositions().count(),
                    pf.getName());
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Map<PositionKey, Position> positionMap =
                            pf.getPositions().collect(Collectors.toMap(p -> p.getKey(), p -> p));
                    positionCacheStore.writeAll(positionMap);
                }
            });
        });

        PortfolioCacheStore portfolioCacheStore = beanContext.getBean(PortfolioCacheStore.class);
        // persist the benchmarks first
        LOG.info("persisting 4 benchmark Portfolios");
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                portfolioCacheStore.write(PimcoBenchmarkDataSource.EMAD_KEY,
                        pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.EMAD_KEY));
                portfolioCacheStore.write(PimcoBenchmarkDataSource.GLAD_KEY,
                        pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.GLAD_KEY));
                portfolioCacheStore.write(PimcoBenchmarkDataSource.ILAD_KEY,
                        pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.ILAD_KEY));
                portfolioCacheStore.write(PimcoBenchmarkDataSource.PGOV_KEY,
                        pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.PGOV_KEY));
            }
        });
        portfolios.stream().filter(p -> p.getKey().getId().length() != 4).forEach(p -> {
            LOG.info("persisting Portfolio {}", p.getKey());
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    portfolioCacheStore.write(p.getKey(), p);
                }
            });
        });

        LOG.info("completed database loading");
    }
}
