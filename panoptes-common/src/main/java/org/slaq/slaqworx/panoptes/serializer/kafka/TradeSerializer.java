package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.trade.Trade;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link Trade}.
 *
 * @author jeremy
 */
public class TradeSerializer extends KafkaSerializer<Trade> {
  /** Creates a new {@link TradeSerializer}. Kafka requires a public default constructor. */
  public TradeSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TradeSerializer());
  }
}
