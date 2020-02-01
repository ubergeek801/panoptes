package org.slaq.slaqworx.panoptes.rule;

import java.util.Collections;
import java.util.Map;

import org.slaq.slaqworx.panoptes.asset.SecurityAttributes;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

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
    private final Map<SecurityKey, SecurityAttributes> securityOverrides;

    /**
     * Creates a new {@code EvaluationContext} which performs full (non-short-circuit) {@code Rule}
     * evaluation with no {@code Security} attribute overrides.
     */
    public EvaluationContext() {
        this(EvaluationMode.FULL_EVALUATION, null);
    }

    /**
     * Creates a new {@code EvaluationContext} with the given evaluation mode and no
     * {@code Security} attribute overrides.
     *
     * @param evaluationMode
     *            the evaluation mode in which to evaluate
     */
    public EvaluationContext(EvaluationMode evaluationMode) {
        this(evaluationMode, null);
    }

    /**
     * Creates a new {@code EvaluationContext} with the given evaluation mode and no
     * {@code Security} attribute overrides.
     *
     * @param evaluationMode
     *            the evaluation mode in which to evaluate
     * @param securityAttributeOverrides
     *            a (possibly {@code null} or empty) {@code Map} relating a {@code SecurityKey} to a
     *            {@code SecurityAttributes} which should override the current values for the
     *            purposes of this evaluation
     */
    public EvaluationContext(EvaluationMode evaluationMode,
            Map<SecurityKey, SecurityAttributes> securityAttributeOverrides) {
        this.evaluationMode = evaluationMode;
        securityOverrides = (securityAttributeOverrides == null ? Collections.emptyMap()
                : securityAttributeOverrides);
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
        if (securityOverrides == null) {
            if (other.securityOverrides != null) {
                return false;
            }
        } else if (!securityOverrides.equals(other.securityOverrides)) {
            return false;
        }

        return true;
    }

    /**
     * Obtains the {@code EvaluationMode} in effect for this context.
     *
     * @return a {@code EvaluationMode}
     */
    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    /**
     * Obtains the {@code Security} overrides in effect for the current evaluation.
     *
     * @return a (possibly empty but never {@code null}) {@code Map} relating a {@code SecurityKey}
     *         to a {@code SecurityAttributes} which should override the current values for the
     *         purposes of this evaluation
     */
    public Map<SecurityKey, SecurityAttributes> getSecurityOverrides() {
        return securityOverrides;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((evaluationMode == null) ? 0 : evaluationMode.hashCode());
        result = prime * result + ((securityOverrides == null) ? 0 : securityOverrides.hashCode());

        return result;
    }
}
