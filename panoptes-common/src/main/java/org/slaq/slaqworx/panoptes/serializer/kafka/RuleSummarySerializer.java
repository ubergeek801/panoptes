package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.rule.RuleSummary;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link RuleSummary}.
 *
 * @author jeremy
 */
public class RuleSummarySerializer extends KafkaSerializer<RuleSummary> {
  /** Creates a new {@link RuleSummarySerializer}. Kafka requires a public default constructor. */
  public RuleSummarySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.RuleSummarySerializer());
  }
}
