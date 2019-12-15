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

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.cache.AssetCache;

/**
 * Panoptes is a prototype system for investment portfolio compliance assurance. {@code Panoptes} is
 * the entry point of the application.
 *
 * @author jeremy
 */
@Singleton
@Requires(notEnv = "test")
public class Panoptes {
    private static final Logger LOG = LoggerFactory.getLogger(Panoptes.class);

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
