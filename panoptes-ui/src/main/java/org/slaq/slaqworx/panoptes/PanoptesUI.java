package org.slaq.slaqworx.panoptes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.inject.Singleton;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.ApplicationStartupEvent;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.cache.AssetCache;

/**
 * Panoptes is a prototype system for investment portfolio compliance assurance. {@code PanoptesUI}
 * is the entry point of the Web application.
 *
 * @author jeremy
 */
@Singleton
@Context
@Requires(notEnv = "test")
public class PanoptesUI implements ApplicationEventListener<ApplicationStartupEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(PanoptesUI.class);

    /**
     * The entry point for the Panoptes application.
     *
     * @param args
     *            the program arguments
     */
    public static void main(String[] args) {
        InputStream bannerStream =
                PanoptesUI.class.getClassLoader().getResourceAsStream("banner.txt");
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

        Micronaut.run(PanoptesUI.class, args);
    }

    /**
     * Creates a new instance of the Panoptes UI application.
     */
    protected PanoptesUI() {
        // nothing to do
    }

    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        try {
            @SuppressWarnings("resource") ApplicationContext applicationContext =
                    event.getSource().getApplicationContext();
            AssetCache assetCache = applicationContext.getBean(AssetCache.class);

            int numSecurities = assetCache.getSecurityCache().size();
            LOG.info("{} Securities in cache", numSecurities);

            int numPositions = assetCache.getPositionCache().size();
            LOG.info("{} Positions in cache", numPositions);

            int numRules = assetCache.getRuleCache().size();
            LOG.info("{} Rules in cache", numRules);

            int numPortfolios = assetCache.getPortfolioCache().size();
            LOG.info("{} Portfolios in cache", numPortfolios);

            LOG.info("Panoptes cluster client ready");

            LOG.info("starting Web application service");

            Server vaadinServer =
                    applicationContext.getBean(Server.class, Qualifiers.byName("vaadinServer"));
            vaadinServer.start();

            LOG.info("Panoptes Web application ready");
        } catch (Exception e) {
            // FIXME throw a better exception
            throw new RuntimeException("could not initialize Panoptes", e);
        }
    }
}
