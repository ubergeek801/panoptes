package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.PositionKey;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code PositionKey}.
 *
 * @author jeremy
 */
public class PositionKeySerializer extends KafkaSerializer<PositionKey> {
    /**
     * Creates a new {@code PositionKeySerializer}. Kafka requires a public default constructor.
     */
    public PositionKeySerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.PositionKeySerializer());
    }
}
