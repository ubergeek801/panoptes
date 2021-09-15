package org.slaq.slaqworx.panoptes.mock;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.event.PortfolioCommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A standalone application which publishes random portfolio update events to Kafka.
 *
 * @author jeremy
 */
@Singleton
@Requires(env = {"portfolio-update"})
public class PortfolioUpdater {
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
   *
   * @throws Exception
   *     if any error occurs
   */
  public static void main(String[] args) throws Exception {
    try (ApplicationContext appContext = Micronaut.build(args).mainClass(PortfolioUpdater.class)
        .environments("portfolio-update", "offline").args(args).build().start()) {
      PortfolioUpdater updater = appContext.getBean(PortfolioUpdater.class);
      updater.updatePortfolios();
    }
  }

  /**
   * Publishes random portfolio updates to Kafka.
   */
  public void updatePortfolios() {
    HashSet<PortfolioKey> portfolioKeys = new HashSet<>(800 * 2);
    for (int i = 1; i <= 800; i++) {
      portfolioKeys.add(new PortfolioKey("test" + i, 1));
    }

    for (int i = 0; i < 500; i++) {
      // republish some random portfolios
      ArrayList<PortfolioKey> portfoliosCopy = new ArrayList<>(portfolioKeys);
      HashSet<PortfolioKey> randomPortfolios = new HashSet<>();
      for (int j = 0; j < 100; j++) {
        randomPortfolios.add(portfoliosCopy.remove((int) (Math.random() * portfoliosCopy.size())));
      }

      long[] eventId = new long[] {System.currentTimeMillis()};
      LOG.info("publishing {} portfolios", randomPortfolios.size());
      randomPortfolios.parallelStream().forEach(
          p -> kafkaProducer.publishPortfolioEvent(p, new PortfolioCommandEvent(eventId[0]++, p)));
    }

    LOG.info("published portfolios");
  }
}
