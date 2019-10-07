package org.slaq.slaqworx.panoptes.rule;

import java.io.Serializable;

/**
 * {@code EvaluationContext} provides contextual information related to the execution of
 * {@code Portfolio} evaluation.
 *
 * @author jeremy
 */
public class EvaluationContext implements Serializable {
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

    private static final long serialVersionUID = 1L;

    private final EvaluationMode evaluationMode;

    /**
     * Creates a new {@code EvaluationContext} which performs full (non-short-circuit) {@code Rule}
     * evaluation
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

    /**
     * Obtains the {@code TradeEvaluationMode} in effect for this context.
     *
     * @return a {@code TradeEvaluationMode}
     */
    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }
}
