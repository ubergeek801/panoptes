package org.slaq.slaqworx.panoptes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.inject.Singleton;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;

import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.data.DummyPortfolioMapLoader;
import org.slaq.slaqworx.panoptes.data.PimcoBenchmarkDataSource;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;

/**
 * {@code PanoptesOffline} is the entry point of a Panoptes instance that operates "offline" (i.e.
 * not connected to a database), initializing its cache from randomly generated portfolio data. Runs
 * as a "unit test" so that the test environment and classpath are used.
 *
 * @author jeremy
 */
@Singleton
@Requires(env = "offline")
public class PanoptesOffline {
    private static final Logger LOG = LoggerFactory.getLogger(PanoptesOffline.class);

    /**
     * The entry point for offline mode.
     */
    @Test
    public void main() throws InterruptedException {
        String environments = System.getProperty("micronaut.environments");
        if (environments == null || !Set.of(environments.split(",")).contains("offline")) {
            // we were not meant to run right now
            return;
        }

        Micronaut.run(PanoptesOffline.class);
        while (!Thread.interrupted()) {
            Thread.sleep(10000);
        }
    }

    /**
     * Initializes the cache from dummy data, in much the same way as
     * {@code PimcoBenchmarkDatabaseLoader} initializes the persistent store.
     *
     * @param assetCache
     *            the cache to be initialized
     * @throws IOException
     *             if source data could not be read
     */
    protected void initializeCache(AssetCache assetCache) throws IOException {
        PimcoBenchmarkDataSource pimcoDataSource = PimcoBenchmarkDataSource.getInstance();

        LOG.info("initializing {} Securities", pimcoDataSource.getSecurityMap().size());
        assetCache.getSecurityCache().putAll(pimcoDataSource.getSecurityMap());

        DummyPortfolioMapLoader mapLoader = new DummyPortfolioMapLoader();
        ArrayList<Portfolio> portfolios = new ArrayList<>();
        for (PortfolioKey key : mapLoader.loadAllKeys()) {
            Portfolio portfolio = mapLoader.load(key);
            portfolios.add(portfolio);
        }

        portfolios.stream().forEach(pf -> {
            LOG.info("initializing {} Rules for Portfolio \"{}\"", pf.getRules().count(),
                    pf.getName());
            pf.getRules()
                    .forEach(r -> assetCache.getRuleCache().put(r.getKey(), (ConfigurableRule)r));
        });

        portfolios.stream().forEach(pf -> {
            LOG.info("initializing {} Positions for Portfolio \"{}\"", pf.getPositions().count(),
                    pf.getName());
            pf.getPositions().forEach(p -> assetCache.getPositionCache().put(p.getKey(), p));
        });

        // initialize the benchmarks first
        LOG.info("initializing 4 benchmark Portfolios");
        assetCache.getPortfolioCache().put(PimcoBenchmarkDataSource.EMAD_KEY,
                pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.EMAD_KEY));
        assetCache.getPortfolioCache().put(PimcoBenchmarkDataSource.GLAD_KEY,
                pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.GLAD_KEY));
        assetCache.getPortfolioCache().put(PimcoBenchmarkDataSource.ILAD_KEY,
                pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.ILAD_KEY));
        assetCache.getPortfolioCache().put(PimcoBenchmarkDataSource.PGOV_KEY,
                pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.PGOV_KEY));

        portfolios.stream().filter(p -> p.getKey().getId().length() != 4).forEach(p -> {
            LOG.info("initializing Portfolio {}", p.getKey());
            assetCache.getPortfolioCache().put(p.getKey(), p);
        });

        LOG.info("completed cache initialization");
    }

    /**
     * Initializes the Panoptes Web application upon startup.
     *
     * @param event
     *            a {@code StartupEvent}
     * @throws Exception
     *             if initialization could not be completed
     */
    @EventListener
    protected void onStartup(StartupEvent event) throws Exception {
        @SuppressWarnings("resource")
        BeanContext applicationContext = event.getSource();
        AssetCache assetCache = applicationContext.getBean(AssetCache.class);

        initializeCache(assetCache);

        int numSecurities = assetCache.getSecurityCache().size();
        LOG.info("{} Securities in cache", numSecurities);

        int numPositions = assetCache.getPositionCache().size();
        LOG.info("{} Positions in cache", numPositions);

        int numRules = assetCache.getRuleCache().size();
        LOG.info("{} Rules in cache", numRules);

        int numPortfolios = assetCache.getPortfolioCache().size();
        LOG.info("{} Portfolios in cache", numPortfolios);

        LOG.info("Panoptes cluster node ready");

        LOG.info("starting Web application service");

        Server servletServer = applicationContext.getBean(Server.class);
        servletServer.start();

        LOG.info("Panoptes Web application ready");
    }
}
