package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link TradeKey}.
 *
 * @author jeremy
 */
public class TradeKeySerializer extends KafkaSerializer<TradeKey> {
  /** Creates a new {@link TradeKeySerializer}. Kafka requires a public default constructor. */
  public TradeKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TradeKeySerializer());
  }
}
