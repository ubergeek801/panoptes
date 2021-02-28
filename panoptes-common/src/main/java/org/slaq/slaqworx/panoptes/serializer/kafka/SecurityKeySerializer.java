package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link SecurityKey}.
 *
 * @author jeremy
 */
public class SecurityKeySerializer extends KafkaSerializer<SecurityKey> {
  /**
   * Creates a new {@link SecurityKeySerializer}. Kafka requires a public default constructor.
   */
  public SecurityKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.SecurityKeySerializer());
  }
}
