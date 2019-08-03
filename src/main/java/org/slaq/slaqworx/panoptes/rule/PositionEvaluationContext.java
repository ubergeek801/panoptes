package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * A PositionEvaluationContext can be thought of as the EvaluationContext for a Position, as it is a
 * composite of the two; it mostly exists for the convenience of specifying Position filters as
 * Predicates.
 *
 * @author jeremy
 */
public class PositionEvaluationContext {
    private final Position position;
    private final EvaluationContext evaluationContext;

    /**
     * Creates a new PositionEvaluationContext comprising the given Position and EvaluationContext.
     *
     * @param position
     *            the Position to be evaluated in the context
     * @param evaluationContext
     *            the context in which to evaluate the Position
     */
    public PositionEvaluationContext(Position position, EvaluationContext evaluationContext) {
        this.position = position;
        this.evaluationContext = evaluationContext;
    }

    /**
     * Obtains the EvaluationContext of this context.
     * 
     * @return an EvaluationContext
     */
    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

    /**
     * Obtains the Position of this context.
     *
     * @return a Position
     */
    public Position getPosition() {
        return position;
    }
}
