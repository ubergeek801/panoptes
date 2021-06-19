package org.slaq.slaqworx.panoptes.mock;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.event.PortfolioCommandEvent;
import org.slaq.slaqworx.panoptes.offline.DummyPortfolioMapLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A standalone application which publishes random portfolio update events to Kafka.
 *
 * @author jeremy
 */
@Singleton
@Context
@Requires(env = {"portfolio-update"})
public class PortfolioUpdater implements ApplicationEventListener<StartupEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(PortfolioUpdater.class);
  private final KafkaProducer kafkaProducer;

  /**
   * Creates a {@link PortfolioUpdater} that publishes using the given {@link KafkaProducer}.
   *
   * @param kafkaProducer
   *     the {@link KafkaProducer} with which to publish events to Kafka
   */
  protected PortfolioUpdater(KafkaProducer kafkaProducer) {
    this.kafkaProducer = kafkaProducer;
  }

  /**
   * Executes the {@link PortfolioUpdater} application.
   *
   * @param args
   *     the program arguments (unused)
   */
  public static void main(String[] args) {
    try (ApplicationContext appContext = Micronaut.build(args).mainClass(PortfolioUpdater.class)
        .environments("portfolio-update", "offline").args(args).start()) {
      // nothing else to do
    }
  }

  @Override
  public void onApplicationEvent(StartupEvent event) {
    PortfolioUpdater updater = event.getSource().getBean(PortfolioUpdater.class);
    try {
      updater.updatePortfolios();
    } catch (Exception e) {
      // FIXME throw a better exception
      throw new RuntimeException("could not perform bootstrap", e);
    }
  }

  /**
   * Publishes random portfolio updates to Kafka.
   *
   * @throws IOException
   *     if the data could not be read
   */
  public void updatePortfolios() throws IOException {
    // generate the portfolios
    LOG.info("generating portfolios");
    DummyPortfolioMapLoader mapLoader =
        new DummyPortfolioMapLoader(600);
    ArrayList<Portfolio> portfolios = new ArrayList<>();
    for (PortfolioKey key : mapLoader.loadAllKeys()) {
      Portfolio portfolio = mapLoader.load(key);
      // skip "abstract" portfolios (e.g. benchmarks) since we deal with them elsewhere
      if (!portfolio.isAbstract()) {
        portfolios.add(portfolio);
      }
    }

    long startTime = System.currentTimeMillis();
//    while (System.currentTimeMillis() - startTime < 120_000) {
    for (int j = 0; j < 50; j++) {
      // republish some random portfolios
      ArrayList<Portfolio> portfoliosCopy = new ArrayList<>(portfolios);
      ArrayList<Portfolio> randomPortfolios = new ArrayList<>();
      for (int i = 0; i < 100; i++) {
        randomPortfolios.add(portfoliosCopy.remove((int) (Math.random() * portfoliosCopy.size())));
      }

      long[] eventId = new long[] {System.currentTimeMillis()};
      LOG.info("publishing {} portfolios", randomPortfolios.size());
      randomPortfolios.forEach(p -> kafkaProducer
          .publishPortfolioEvent(p.getKey(), new PortfolioCommandEvent(eventId[0]++, p.getKey())));
    }

    LOG.info("published portfolios");
  }
}
