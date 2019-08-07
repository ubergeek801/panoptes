package org.slaq.slaqworx.panoptes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

import org.slaq.slaqworx.panoptes.data.PortfolioCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.PortfolioEvaluator;

/**
 * Panoptes is a prototype system for investment portfolio compliance assurance.
 *
 * @author jeremy
 */
@SpringBootApplication
public class Panoptes implements ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(Panoptes.class);

    private static ApplicationContext applicationContext;

    /**
     * Obtains the ApplicationContext of the running Panoptes application. This should only be used
     * in cases where dependency injection isn't possible, e.g. from Hazelcast <code>MapStore</code>
     * classes which are instantiated directly by Hazelcast.
     *
     * @return the current ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * The entry point for the Panoptes application. Currently the app doesn't do anything useful.
     *
     * @param args
     *            the program arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Panoptes.class, args);

        PortfolioCache portfolioCache = applicationContext.getBean(PortfolioCache.class);

        portfolioCache.getSecurityCache().loadAll(false);
        int numSecurities = portfolioCache.getSecurityCache().size();
        LOG.info("{} Securities in cache", numSecurities);

        portfolioCache.getPositionCache().loadAll(false);
        int numPositions = portfolioCache.getPositionCache().size();
        LOG.info("{} Positions in cache", numPositions);

        portfolioCache.getRuleCache().loadAll(false);
        int numRules = portfolioCache.getRuleCache().size();
        LOG.info("{} Rules in cache", numRules);

        portfolioCache.getPortfolioCache().loadAll(false);
        int numPortfolios = portfolioCache.getPortfolioCache().size();
        LOG.info("{} Portfolios in cache", numPortfolios);

        long startTime = System.currentTimeMillis();
        PortfolioEvaluator evaluator = new PortfolioEvaluator();
        portfolioCache.getPortfolioCache().values().forEach(p -> {
            LOG.info("evaluating Portfolio {}", p.getName());
            evaluator.evaluate(p,
                    new EvaluationContext(portfolioCache, portfolioCache, portfolioCache));
        });
        long endTime = System.currentTimeMillis();
        LOG.info("evaluated {} Portfolios in {} ms", numPortfolios, endTime - startTime);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Panoptes.applicationContext = applicationContext;
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
            LOG.info("Panoptes started");
        };
    }
}
