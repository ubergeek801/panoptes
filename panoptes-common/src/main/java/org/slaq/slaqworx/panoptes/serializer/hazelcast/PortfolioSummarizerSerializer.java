package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.cache.PortfolioSummarizer;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link
 * PortfolioSummarizer}.
 *
 * @author jeremy
 */
public class PortfolioSummarizerSerializer extends HazelcastStreamSerializer<PortfolioSummarizer> {
  /**
   * Creates a new {@link PortfolioSummarizerSerializer}. Hazelcast requires a public default
   * constructor.
   */
  public PortfolioSummarizerSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioSummarizerSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.PORTFOLIO_SUMMARIZER.ordinal();
  }
}
