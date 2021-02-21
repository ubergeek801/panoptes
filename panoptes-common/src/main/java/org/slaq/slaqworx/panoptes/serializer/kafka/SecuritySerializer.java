package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.Security;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code Security}.
 *
 * @author jeremy
 */
public class SecuritySerializer extends KafkaSerializer<Security> {
    /**
     * Creates a new {@code SecuritySerializer}. Kafka requires a public default constructor.
     */
    public SecuritySerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.SecuritySerializer());
    }
}
