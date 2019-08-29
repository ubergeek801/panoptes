package org.slaq.slaqworx.panoptes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.inject.Singleton;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequestListener;

/**
 * Panoptes is a prototype system for investment portfolio compliance assurance.
 *
 * @author jeremy
 */
@Singleton
@Requires(notEnv = "TEST")
public class Panoptes {
    private static final Logger LOG = LoggerFactory.getLogger(Panoptes.class);

    private static BeanContext applicationContext;

    /**
     * Obtains the {@code ApplicationContext} of the running Panoptes application. This should only
     * be used in cases where dependency injection isn't possible, e.g. from Hazelcast
     * {@code MapStore} classes which are instantiated directly by Hazelcast.
     *
     * @return the current ApplicationContext
     */
    public static BeanContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * The entry point for the Panoptes application.
     *
     * @param args
     *            the program arguments
     */
    public static void main(String[] args) {
        InputStream bannerStream =
                Panoptes.class.getClassLoader().getResourceAsStream("banner.txt");
        if (bannerStream != null) {
            try (BufferedReader bannerReader =
                    new BufferedReader(new InputStreamReader(bannerStream))) {
                String line;
                while ((line = bannerReader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                // never mind
            }
        }

        Micronaut.run(Panoptes.class, args);
    }

    /**
     * Creates a new instance of the Panoptes application.
     */
    protected Panoptes() {
        // nothing to do
    }

    /**
     * Initializes the Panoptes application upon startup.
     *
     * @param event
     *            a {@code StartupEvent}
     */
    @EventListener
    protected void onStartup(StartupEvent event) {
        applicationContext = event.getSource();
        AssetCache assetCache = applicationContext.getBean(AssetCache.class);

        assetCache.getSecurityCache().loadAll(false);
        int numSecurities = assetCache.getSecurityCache().size();
        LOG.info("{} Securities in cache", numSecurities);

        assetCache.getPositionCache().loadAll(false);
        int numPositions = assetCache.getPositionCache().size();
        LOG.info("{} Positions in cache", numPositions);

        assetCache.getRuleCache().loadAll(false);
        int numRules = assetCache.getRuleCache().size();
        LOG.info("{} Rules in cache", numRules);

        assetCache.getPortfolioCache().loadAll(false);
        int numPortfolios = assetCache.getPortfolioCache().size();
        LOG.info("{} Portfolios in cache", numPortfolios);

        LOG.info("starting PortfolioEvaluationRequestListener");
        PortfolioEvaluationRequestListener portfolioEvaluationRequestListener =
                applicationContext.getBean(PortfolioEvaluationRequestListener.class);
        portfolioEvaluationRequestListener.start();

        LOG.info("Panoptes ready");
    }
}
