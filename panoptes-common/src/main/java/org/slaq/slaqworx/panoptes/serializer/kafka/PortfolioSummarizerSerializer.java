package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.cache.PortfolioSummarizer;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link PortfolioSummarizer}.
 *
 * @author jeremy
 */
public class PortfolioSummarizerSerializer extends KafkaSerializer<PortfolioSummarizer> {
  /**
   * Creates a new {@link PortfolioSummarizerSerializer}. Kafka requires a public default
   * constructor.
   */
  public PortfolioSummarizerSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioSummarizerSerializer());
  }
}
