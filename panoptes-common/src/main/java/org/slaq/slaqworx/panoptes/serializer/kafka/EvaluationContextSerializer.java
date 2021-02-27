package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of an {@code EvaluationContext}.
 *
 * @author jeremy
 */
public class EvaluationContextSerializer extends KafkaSerializer<EvaluationContext> {
  /**
   * Creates a new {@code EvaluationContextSerializer}. Kafka requires a public default
   * constructor.
   */
  public EvaluationContextSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.EvaluationContextSerializer());
  }
}
