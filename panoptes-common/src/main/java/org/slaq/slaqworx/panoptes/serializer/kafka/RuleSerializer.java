package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link Rule} (actually a {@link
 * ConfigurableRule}).
 *
 * @author jeremy
 */
public class RuleSerializer extends KafkaSerializer<ConfigurableRule> {
  /** Creates a new {@link RuleSerializer}. Kafka requires a public default constructor. */
  public RuleSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.RuleSerializer());
  }
}
