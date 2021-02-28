package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link PortfolioKey}.
 *
 * @author jeremy
 */
public class PortfolioKeySerializer extends KafkaSerializer<PortfolioKey> {
  /**
   * Creates a new {@link PortfolioKeySerializer}. Kafka requires a public default constructor.
   */
  public PortfolioKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioKeySerializer());
  }
}
