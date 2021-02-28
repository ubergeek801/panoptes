package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link RuleEvaluationResult}.
 *
 * @author jeremy
 */
public class RuleEvaluationResultSerializer extends KafkaSerializer<RuleEvaluationResult> {
  /**
   * Creates a new {@link RuleEvaluationResultSerializer}. Kafka requires a public default
   * constructor.
   */
  public RuleEvaluationResultSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.RuleEvaluationResultSerializer());
  }
}
