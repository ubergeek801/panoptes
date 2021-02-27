package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code SecurityKey}.
 *
 * @author jeremy
 */
public class SecurityKeySerializer extends KafkaSerializer<SecurityKey> {
  /**
   * Creates a new {@code SecurityKeySerializer}. Kafka requires a public default constructor.
   */
  public SecurityKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.SecurityKeySerializer());
  }
}
