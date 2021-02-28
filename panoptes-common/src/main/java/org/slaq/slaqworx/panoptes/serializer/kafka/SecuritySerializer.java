package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.Security;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link Security}.
 *
 * @author jeremy
 */
public class SecuritySerializer extends KafkaSerializer<Security> {
  /**
   * Creates a new {@link SecuritySerializer}. Kafka requires a public default constructor.
   */
  public SecuritySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.SecuritySerializer());
  }
}
