package org.slaq.slaqworx.panoptes.mock;

import java.io.IOException;
import java.util.ArrayList;

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
import org.slaq.slaqworx.panoptes.event.PortfolioCommandEvent;
import org.slaq.slaqworx.panoptes.offline.DummyPortfolioMapLoader;

@Singleton
@Context
@Requires(env = { "portfolio-update" })
public class PortfolioUpdater implements ApplicationEventListener<StartupEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(PortfolioUpdater.class);

    /**
     * Executes the {@code Bootstrapper} application.
     *
     * @param args
     *            the program arguments (unused)
     */
    public static void main(String[] args) {
        try (ApplicationContext appContext = Micronaut.build(args).mainClass(PortfolioUpdater.class)
                .environments("portfolio-update", "offline").args(args).start()) {
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
    protected PortfolioUpdater(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        PortfolioUpdater bootstrapper = event.getSource().getBean(PortfolioUpdater.class);
        try {
            bootstrapper.updatePortfolios();
        } catch (Exception e) {
            // FIXME throw a better exception
            throw new RuntimeException("could not perform bootstrap", e);
        }
    }

    /**
     * Publishes random portfolio updates to Kafka.
     *
     * @throws IOException
     *             if the data could not be read
     */
    public void updatePortfolios() throws IOException {
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

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 120000) {
            // republish some random portfolios
            ArrayList<Portfolio> portfoliosCopy = new ArrayList<>(portfolios);
            ArrayList<Portfolio> randomPortfolios = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                randomPortfolios
                        .add(portfoliosCopy.remove((int)(Math.random() * portfoliosCopy.size())));
            }

            long[] eventId = new long[] { System.currentTimeMillis() };
            LOG.info("publishing {} portfolios", randomPortfolios.size());
            randomPortfolios.forEach(p -> kafkaProducer.publishPortfolioEvent(p.getKey(),
                    new PortfolioCommandEvent(eventId[0]++, p.getKey())));
        }

        LOG.info("published portfolios");
    }
}
