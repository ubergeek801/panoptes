package org.slaq.slaqworx.panoptes.mock;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.offline.PimcoBenchmarkDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A standalone application which publishes random security update events to Kafka.
 *
 * @author jeremy
 */
@Singleton
@Requires(env = {"security-update"})
public class SecurityUpdater {
  private static final Logger LOG = LoggerFactory.getLogger(SecurityUpdater.class);
  private final KafkaProducer kafkaProducer;

  /**
   * Creates a {@link SecurityUpdater} that publishes using the given {@link KafkaProducer}.
   *
   * @param kafkaProducer the {@link KafkaProducer} with which to publish events to Kafka
   */
  protected SecurityUpdater(KafkaProducer kafkaProducer) {
    this.kafkaProducer = kafkaProducer;
  }

  /**
   * Executes the {@link SecurityUpdater} application.
   *
   * @param args the program arguments (unused)
   * @throws Exception if any error occurs
   */
  public static void main(String[] args) throws Exception {
    try (ApplicationContext appContext =
        Micronaut.build(args)
            .mainClass(SecurityUpdater.class)
            .environments("security-update", "offline")
            .args(args)
            .build()
            .start()) {
      SecurityUpdater updater = appContext.getBean(SecurityUpdater.class);
      updater.updateSecurities();
    }
  }

  /**
   * Publishes random security updates to Kafka.
   *
   * @throws IOException if the data could not be read
   */
  public void updateSecurities() throws IOException {
    // republish some random securities
    ArrayList<Security> securities =
        new ArrayList<>(PimcoBenchmarkDataSource.getInstance().getSecurityMap().values());
    ArrayList<Security> randomSecurities = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      randomSecurities.add(securities.remove((int) (Math.random() * securities.size())));
    }

    LOG.info("publishing {} securities", randomSecurities.size());
    randomSecurities.forEach(s -> kafkaProducer.publishSecurity(s.getKey(), s));
    LOG.info("published securities");
  }
}
