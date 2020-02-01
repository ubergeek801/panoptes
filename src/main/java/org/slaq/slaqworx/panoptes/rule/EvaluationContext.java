package org.slaq.slaqworx.panoptes.rule;

import java.util.Collections;
import java.util.Map;

import org.slaq.slaqworx.panoptes.asset.SecurityAttributes;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;

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

    private final SecurityProvider securityProvider;
    private final EvaluationMode evaluationMode;
    private final Map<SecurityKey, SecurityAttributes> securityOverrides;

    /**
     * Creates a new {@code EvaluationContext} which performs full (non-short-circuit) {@code Rule}
     * evaluation, uses the given {@code SecurityProvider} to resolve {@code Security} references,
     * and which supplies no {@code Security} attribute overrides.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Security} references
     */
    public EvaluationContext(SecurityProvider securityProvider) {
        this(securityProvider, EvaluationMode.FULL_EVALUATION);
    }

    /**
     * Creates a new {@code EvaluationContext} which uses the given evaluation mode, uses the given
     * {@code SecurityProvider} to resolve {@code Security} references, and which supplies no
     * {@code Security} attribute overrides.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Security} references
     * @param evaluationMode
     *            the evaluation mode in which to evaluate
     */
    public EvaluationContext(SecurityProvider securityProvider, EvaluationMode evaluationMode) {
        this(securityProvider, evaluationMode, null);
    }

    /**
     * Creates a new {@code EvaluationContext} which uses the given evaluation mode, uses the given
     * {@code SecurityProvider} to resolve {@code Security} references, and which specifies
     * attributes which should override current {@code Security} attribute values for the purposes
     * of the current evaluation.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Security} references
     * @param evaluationMode
     *            the evaluation mode in which to evaluate
     * @param securityAttributeOverrides
     *            a (possibly {@code null} or empty) {@code Map} relating a {@code SecurityKey} to a
     *            {@code SecurityAttributes} which should override the current values
     */
    public EvaluationContext(SecurityProvider securityProvider, EvaluationMode evaluationMode,
            Map<SecurityKey, SecurityAttributes> securityAttributeOverrides) {
        this.evaluationMode = evaluationMode;
        this.securityProvider = securityProvider;
        securityOverrides = (securityAttributeOverrides == null ? Collections.emptyMap()
                : securityAttributeOverrides);
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj);
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

    /**
     * Obtains the {@code SecurityProvider} in effect for the current evaluation.
     *
     * @return a {@code SecurityProvider}
     */
    public SecurityProvider getSecurityProvider() {
        return securityProvider;
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
