package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of an {@link EvaluationContext}.
 *
 * @author jeremy
 */
public class EvaluationContextSerializer extends KafkaSerializer<EvaluationContext> {
  /**
   * Creates a new {@link EvaluationContextSerializer}. Kafka requires a public default constructor.
   */
  public EvaluationContextSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.EvaluationContextSerializer());
  }
}
