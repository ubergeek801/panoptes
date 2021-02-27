package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.asset.Portfolio;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a {@code Portfolio}.
 *
 * @author jeremy
 */
public class PortfolioSerializer extends HazelcastStreamSerializer<Portfolio> {
  /**
   * Creates a new {@code PortfolioSerializer}. Hazelcast requires a public default constructor.
   */
  public PortfolioSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.PORTFOLIO.ordinal();
  }
}
