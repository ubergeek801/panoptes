package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link TradeKey}.
 *
 * @author jeremy
 */
public class TradeKeySerializer extends HazelcastStreamSerializer<TradeKey> {
  /** Creates a new {@link TradeKeySerializer}. Hazelcast requires a public default constructor. */
  public TradeKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TradeKeySerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.TRADE_KEY.ordinal();
  }
}
