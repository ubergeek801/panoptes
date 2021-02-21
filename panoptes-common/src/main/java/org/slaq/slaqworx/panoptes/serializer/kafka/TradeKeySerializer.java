package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code TradeKey}.
 *
 * @author jeremy
 */
public class TradeKeySerializer extends KafkaSerializer<TradeKey> {
    /**
     * Creates a new {@code TradeKeySerializer}. Kafka requires a public default constructor.
     */
    public TradeKeySerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.TradeKeySerializer());
    }
}
