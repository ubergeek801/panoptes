package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link
 * PortfolioSummary}.
 *
 * @author jeremy
 */
public class PortfolioSummarySerializer extends HazelcastStreamSerializer<PortfolioSummary> {
  /**
   * Creates a new {@link PortfolioSummarySerializer}. Hazelcast requires a public default
   * constructor.
   */
  public PortfolioSummarySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioSummarySerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.PORTFOLIO_SUMMARY.ordinal();
  }
}
