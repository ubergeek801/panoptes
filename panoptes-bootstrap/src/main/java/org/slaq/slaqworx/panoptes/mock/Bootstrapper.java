package org.slaq.slaqworx.panoptes.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.inject.Singleton;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.offline.DummyPortfolioMapLoader;
import org.slaq.slaqworx.panoptes.offline.PimcoBenchmarkDataSource;

/**
 * "Bootstraps" a Kafka-aware Panoptes instance by publishing the "seed" data (e.g. start-of-day
 * portfolio positions) to the appropriate Kafka topics.
 *
 * @author jeremy
 */
@Singleton
@Context
@Requires(env = { "bootstrap" })
public class Bootstrapper implements ApplicationEventListener<StartupEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(Bootstrapper.class);

    /**
     * Executes the {@code Bootstrapper} application.
     *
     * @param args
     *            the program arguments (unused)
     */
    public static void main(String[] args) {
        try (ApplicationContext appContext = Micronaut.build(args).mainClass(Bootstrapper.class)
                .environments("bootstrap", "offline").args(args).start()) {
            // nothing else to do
        }
    }

    private final KafkaProducer kafkaProducer;

    /**
     * Creates a {@code Bootstrapper} that publishes using the given {@code KafkaProducer}.
     *
     * @param kafkaProducer
     *            the {@code KafkaProducer} with which to publish events to Kafka
     */
    protected Bootstrapper(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Bootstraps the appropriate seed data via Kafka.
     *
     * @throws IOException
     *             if the data could not be read
     */
    public void bootstrap() throws IOException {
        bootstrapSecurities();
        bootstrapBenchmarks();
        bootstrapPortfolios();
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        Bootstrapper bootstrapper = event.getSource().getBean(Bootstrapper.class);
        try {
            bootstrapper.bootstrap();
        } catch (Exception e) {
            // FIXME throw a better exception
            throw new RuntimeException("could not perform bootstrap", e);
        }
    }

    /**
     * Bootstraps the seed benchmark data.
     *
     * @throws IOException
     *             if the data could not be read
     */
    protected void bootstrapBenchmarks() throws IOException {
        // simply publish all known benchmarks to Kafka
        Map<PortfolioKey, Portfolio> benchmarks =
                PimcoBenchmarkDataSource.getInstance().getBenchmarkMap();

        LOG.info("publishing {} benchmarks", benchmarks.size());
        benchmarks.forEach(
                (k, b) -> kafkaProducer.publishBenchmarkEvent(k, new PortfolioDataEvent(b)));
        LOG.info("published benchmarks");
    }

    /**
     * Bootstraps the seed portfolio data.
     *
     * @throws IOException
     *             if the data could not be read
     */
    protected void bootstrapPortfolios() throws IOException {
        // generate the portfolios
        LOG.info("generating portfolios");
        DummyPortfolioMapLoader mapLoader = new DummyPortfolioMapLoader(800);
        ArrayList<Portfolio> portfolios = new ArrayList<>();
        for (PortfolioKey key : mapLoader.loadAllKeys()) {
            Portfolio portfolio = mapLoader.load(key);
            // skip "abstract" portfolios (e.g. benchmarks) since we deal with them elsewhere
            if (!portfolio.isAbstract()) {
                portfolios.add(portfolio);
            }
        }

        LOG.info("publishing {} portfolios", portfolios.size());
        portfolios.forEach(
                p -> kafkaProducer.publishPortfolioEvent(p.getKey(), new PortfolioDataEvent(p)));
        LOG.info("published portfolios");
    }

    /**
     * Bootstraps the seed security data.
     *
     * @throws IOException
     *             if the data could not be read
     */
    protected void bootstrapSecurities() throws IOException {
        // simply publish all known securities to Kafka
        Map<SecurityKey, Security> securities =
                PimcoBenchmarkDataSource.getInstance().getSecurityMap();

        LOG.info("publishing {} securities", securities.size());
        securities.forEach((k, s) -> kafkaProducer.publishSecurity(k, s));
        LOG.info("published securities");
    }
}
