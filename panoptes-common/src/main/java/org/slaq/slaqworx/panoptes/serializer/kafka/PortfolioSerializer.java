package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.Portfolio;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code Portfolio}.
 *
 * @author jeremy
 */
public class PortfolioSerializer extends KafkaSerializer<Portfolio> {
  /**
   * Creates a new {@code PortfolioSerializer}. Kafka requires a public default constructor.
   */
  public PortfolioSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioSerializer());
  }
}
