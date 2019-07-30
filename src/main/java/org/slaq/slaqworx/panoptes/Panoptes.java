package org.slaq.slaqworx.panoptes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastJpaDependencyAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import org.slaq.slaqworx.panoptes.data.PortfolioCache;

/**
 * Panoptes is a prototype system for investment portfolio compliance assurance.
 *
 * @author jeremy
 */
@SpringBootApplication(
        // we use JPA to load Hazelcast, so don't use Hazelcast for JPA 2LC
        exclude = HazelcastJpaDependencyAutoConfiguration.class)
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

            LOG.info("cache contains {} Portfolios", portfolioCache.getPortfolioCache().size());
            LOG.info("cache contains {} Securities", portfolioCache.getSecurityCache().size());
        };
    }
}
