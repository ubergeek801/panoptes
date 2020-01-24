package org.slaq.slaqworx.panoptes.rule;

/**
 * {@code EvaluationContext} provides contextual information related to the execution of
 * {@code Portfolio} evaluation.
 *
 * @author jeremy
 */
public class EvaluationContext {
    /**
     * {@code EvaluationMode} specifies behaviors to be observed during evaluation.
     */
    public enum EvaluationMode {
        /**
         * all Rules are evaluated regardless of outcome
         */
        FULL_EVALUATION,
        /**
         * Rule evaluation may be short-circuited if an evaluation fails
         */
        SHORT_CIRCUIT_EVALUATION
    }

    private final EvaluationMode evaluationMode;

    /**
     * Creates a new {@code EvaluationContext} which performs full (non-short-circuit) {@code Rule}
     * evaluation.
     */
    public EvaluationContext() {
        this(EvaluationMode.FULL_EVALUATION);
    }

    /**
     * Creates a new {@code EvaluationContext} with the given attributes.
     *
     * @param evaluationMode
     *            the evaluation mode in which to evaluate
     */
    public EvaluationContext(EvaluationMode evaluationMode) {
        this.evaluationMode = evaluationMode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EvaluationContext other = (EvaluationContext)obj;
        if (evaluationMode != other.evaluationMode) {
            return false;
        }
        return true;
    }

    /**
     * Obtains the {@code TradeEvaluationMode} in effect for this context.
     *
     * @return a {@code TradeEvaluationMode}
     */
    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((evaluationMode == null) ? 0 : evaluationMode.hashCode());
        return result;
    }
}
