package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link PortfolioSummary}.
 *
 * @author jeremy
 */
public class PortfolioSummarySerializer extends KafkaSerializer<PortfolioSummary> {
  /**
   * Creates a new {@link PortfolioSummarySerializer}. Kafka requires a public default constructor.
   */
  public PortfolioSummarySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioSummarySerializer());
  }
}
