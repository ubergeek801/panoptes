package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code Rule} (actually a
 * {@code ConfigurableRule}).
 *
 * @author jeremy
 */
public class RuleSerializer extends KafkaSerializer<ConfigurableRule> {
    /**
     * Creates a new {@code RuleSerializer}. Kafka requires a public default constructor.
     */
    public RuleSerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.RuleSerializer());
    }
}
