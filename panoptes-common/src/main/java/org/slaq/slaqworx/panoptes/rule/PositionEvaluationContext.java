package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * An {@code EvaluationContext} wrapper which can be thought of as the {@code EvaluationContext} for
 * a {@code Position}, as it is a composite of the two; it mostly exists for the convenience of
 * specifying {@code Position} filters as {@code Predicate}s. Unlike an {@code EvaluationContext}
 * which may be used for an entire {@code Portfolio} evaluation, a {@code PositionEvaluationContext}
 * is unique to the {@code Position} being evaluated and may contain additional state about the
 * evaluation.
 *
 * @author jeremy
 */
public class PositionEvaluationContext {
    private final Position position;
    private final EvaluationContext evaluationContext;

    private Throwable exception;

    /**
     * Creates a new {@code PositionEvaluationContext} comprising the given {@code Position} and
     * {@code EvaluationContext}.
     *
     * @param position
     *            the {@code Position} to be evaluated in the context
     * @param evaluationContext
     *            the context in which to evaluate the {@code Position}
     */
    public PositionEvaluationContext(Position position, EvaluationContext evaluationContext) {
        this.position = position;
        this.evaluationContext = evaluationContext;
    }

    /**
     * Obtains the {@code EvaluationContext} of this context.
     *
     * @return an {@code EvaluationContext}
     */
    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

    /**
     * Obtains the exception that occurred during evaluation, if any.
     *
     * @return a (possibly {@code null}) exception
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Obtains the {@code Position} of this context.
     *
     * @return a {@code Position}
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Indicates that an exception occurred during evaluation.
     *
     * @param exception
     *            the exception that occurred during evaluation
     */
    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
