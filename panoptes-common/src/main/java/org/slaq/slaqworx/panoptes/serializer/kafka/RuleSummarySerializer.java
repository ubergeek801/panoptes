package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.rule.RuleSummary;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code RuleSummary}.
 *
 * @author jeremy
 */
public class RuleSummarySerializer extends KafkaSerializer<RuleSummary> {
    /**
     * Creates a new {@code RuleSummarySerializer}. Kafka requires a public default constructor.
     */
    public RuleSummarySerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.RuleSummarySerializer());
    }
}
