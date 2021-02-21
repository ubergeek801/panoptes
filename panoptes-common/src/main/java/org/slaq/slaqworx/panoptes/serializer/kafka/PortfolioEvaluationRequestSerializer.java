package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code PortfolioEvaluationRequest}.
 *
 * @author jeremy
 */
public class PortfolioEvaluationRequestSerializer
        extends KafkaSerializer<PortfolioEvaluationRequest> {
    /**
     * Creates a new {@code PortfolioEvaluationRequestSerializer}. Kafka requires a public default
     * constructor.
     */
    public PortfolioEvaluationRequestSerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.PortfolioEvaluationRequestSerializer());
    }
}
