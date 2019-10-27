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

import org.apache.ignite.Ignite;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.cache.ClusterReadyEvent;
import org.slaq.slaqworx.panoptes.data.IgniteCacheStore;
import org.slaq.slaqworx.panoptes.data.PortfolioCacheStore;
import org.slaq.slaqworx.panoptes.data.PositionCacheStore;
import org.slaq.slaqworx.panoptes.data.RuleCacheStore;
import org.slaq.slaqworx.panoptes.data.SecurityCacheStore;

/**
 * Panoptes is a prototype system for investment portfolio compliance assurance.
 *
 * @author jeremy
 */
@Singleton
@Requires(notEnv = "test")
public class Panoptes {
    private static final Logger LOG = LoggerFactory.getLogger(Panoptes.class);

    private static boolean isMain = false;

    /**
     * The entry point for the Panoptes application.
     *
     * @param args
     *            the program arguments
     */
    public static void main(String[] args) {
        isMain = true;

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

    private boolean isClusterInitialized = false;
    private boolean isWebAppInitialized = false;

    /**
     * Creates a new instance of the Panoptes application.
     */
    protected Panoptes() {
        // nothing to do
    }

    /**
     * Loads the cache associated with the given {@code IgniteCacheStore}.
     *
     * @return the number of items loaded
     */
    protected int loadCache(IgniteCacheStore<?, ?> cacheStore) {
        return cacheStore.loadAll();
    }

    /**
     * Initializes the Panoptes cache node upon cluster readiness.
     *
     * @param event
     *            a {@code ClusterReadyEvent}
     */
    @EventListener
    protected void onClusterReady(ClusterReadyEvent event) {
        if (!isMain) {
            return;
        }

        if (isClusterInitialized) {
            return;
        }
        isClusterInitialized = true;

        // the ApplicationContext should be accessible when an event of this type occurs
        @SuppressWarnings("resource")
        BeanContext applicationContext = ApplicationContextProvider.getApplicationContext();

        LOG.info("initializing cache data");

        int numSecurities = loadCache(applicationContext.getBean(SecurityCacheStore.class));
        LOG.info("{} Securities in cache", numSecurities);

        int numPositions = loadCache(applicationContext.getBean(PositionCacheStore.class));
        LOG.info("{} Positions in cache", numPositions);

        int numRules = loadCache(applicationContext.getBean(RuleCacheStore.class));
        LOG.info("{} Rules in cache", numRules);

        int numPortfolios = loadCache(applicationContext.getBean(PortfolioCacheStore.class));
        LOG.info("{} Portfolios in cache", numPortfolios);

        LOG.info("Panoptes cluster node ready");
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
        if (!isMain) {
            return;
        }

        if (isWebAppInitialized) {
            return;
        }
        isWebAppInitialized = true;

        @SuppressWarnings("resource")
        BeanContext applicationContext = event.getSource();
        ApplicationContextProvider.setApplicationContext(applicationContext);

        LOG.info("starting Web application service");

        Server servletServer = applicationContext.getBean(Server.class);
        servletServer.start();

        LOG.info("Panoptes Web application ready");

        // cause Ignite to be initialized if it hasn't by now
        applicationContext.getBean(Ignite.class);
    }
}
