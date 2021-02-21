package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of an {@code EvaluationResult}.
 *
 * @author jeremy
 */
public class EvaluationResultSerializer extends KafkaSerializer<EvaluationResult> {
    /**
     * Creates a new {@code EvaluationResultSerializer}. Kafka requires a public default
     * constructor.
     */
    public EvaluationResultSerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.EvaluationResultSerializer());
    }
}
