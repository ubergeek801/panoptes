package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link PortfolioKey}.
 *
 * @author jeremy
 */
public class PortfolioKeySerializer extends HazelcastStreamSerializer<PortfolioKey> {
  /**
   * Creates a new {@link PortfolioKeySerializer}. Hazelcast requires a public default constructor.
   */
  public PortfolioKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioKeySerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.PORTFOLIO_KEY.ordinal();
  }
}
