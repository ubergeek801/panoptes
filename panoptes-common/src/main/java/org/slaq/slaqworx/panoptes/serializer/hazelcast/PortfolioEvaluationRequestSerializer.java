package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a
 * {@code PortfolioEvaluationRequest}.
 *
 * @author jeremy
 */
public class PortfolioEvaluationRequestSerializer
        extends HazelcastStreamSerializer<PortfolioEvaluationRequest> {
    /**
     * Creates a new {@code PortfolioEvaluationRequestSerializer}. Hazelcast requires a public
     * default constructor.
     */
    public PortfolioEvaluationRequestSerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.PortfolioEvaluationRequestSerializer());
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.PORTFOLIO_EVALUATION_REQUEST.ordinal();
    }
}
