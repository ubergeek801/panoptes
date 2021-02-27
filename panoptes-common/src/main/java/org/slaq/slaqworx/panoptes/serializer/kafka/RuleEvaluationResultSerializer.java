package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code RuleEvaluationResult}.
 *
 * @author jeremy
 */
public class RuleEvaluationResultSerializer extends KafkaSerializer<RuleEvaluationResult> {
  /**
   * Creates a new {@code RuleEvaluationResultSerializer}. Kafka requires a public default
   * constructor.
   */
  public RuleEvaluationResultSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.RuleEvaluationResultSerializer());
  }
}
