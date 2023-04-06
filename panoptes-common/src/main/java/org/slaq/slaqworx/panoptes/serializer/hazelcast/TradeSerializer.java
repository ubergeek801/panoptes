package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.trade.Trade;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link Trade}.
 *
 * @author jeremy
 */
public class TradeSerializer extends HazelcastStreamSerializer<Trade> {
  /** Creates a new {@link TradeSerializer}. Hazelcast requires a public default constructor. */
  public TradeSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TradeSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.TRADE.ordinal();
  }
}
