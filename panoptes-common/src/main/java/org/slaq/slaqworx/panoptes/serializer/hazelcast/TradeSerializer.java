package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.trade.Trade;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a {@code Trade}.
 *
 * @author jeremy
 */
public class TradeSerializer extends HazelcastStreamSerializer<Trade> {
  /**
   * Creates a new {@code TradeSerializer}. Hazelcast requires a public default constructor.
   */
  public TradeSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TradeSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.TRADE.ordinal();
  }
}
