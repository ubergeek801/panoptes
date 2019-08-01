package org.slaq.slaqworx.panoptes;

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
import org.slaq.slaqworx.panoptes.data.PortfolioCache;

/**
 * Panoptes is a prototype system for investment portfolio compliance assurance.
 *
 * @author jeremy
 */
@SpringBootApplication
public class Panoptes {
    private static final Logger LOG = LoggerFactory.getLogger(Panoptes.class);

    /**
     * The entry point for the Panoptes application. Currently the app doesn't do anything useful.
     *
     * @param args
     *            the program arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Panoptes.class, args);
    }

    /**
     * Provides an ApplicationRunner to be executed upon Panoptes startup.
     *
     * @param appContext
     *            the Spring ApplicationContext
     * @return an ApplicationRunner which initializes Panoptes
     */
    @Bean
    public ApplicationRunner startupRunner(ApplicationContext appContext) {
        return args -> {
            PortfolioCache portfolioCache = appContext.getBean(PortfolioCache.class);

            IMap<PortfolioKey, Portfolio> portfolioMap =
                    (IMap<PortfolioKey, Portfolio>)portfolioCache.getPortfolioCache();
            IMap<PositionKey, Position> positionMap =
                    (IMap<PositionKey, Position>)portfolioCache.getPositionCache();
            IMap<SecurityKey, Security> securityMap =
                    (IMap<SecurityKey, Security>)portfolioCache.getSecurityCache();

            LOG.info("cache contains {} Portfolios", portfolioMap.size());
            LOG.info("cache contains {} Positions", positionMap.size());
            LOG.info("cache contains {} Securities", securityMap.size());
        };
    }
}
