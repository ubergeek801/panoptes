package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code RuleKey}.
 *
 * @author jeremy
 */
public class RuleKeySerializer extends KafkaSerializer<RuleKey> {
  /**
   * Creates a new {@code RuleKeySerializer}. Kafka requires a public default constructor.
   */
  public RuleKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.RuleKeySerializer());
  }
}
