package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.Portfolio;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link Portfolio}.
 *
 * @author jeremy
 */
public class PortfolioSerializer extends KafkaSerializer<Portfolio> {
  /** Creates a new {@link PortfolioSerializer}. Kafka requires a public default constructor. */
  public PortfolioSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioSerializer());
  }
}
