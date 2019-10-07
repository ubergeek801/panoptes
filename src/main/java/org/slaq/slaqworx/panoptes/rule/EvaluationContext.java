package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;

/**
 * {@code EvaluationContext} provides contextual information that can be shared across multiple
 * evaluations.
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

    private final PortfolioProvider portfolioProvider;
    private final SecurityProvider securityProvider;
    private final RuleProvider ruleProvider;
    private final EvaluationMode evaluationMode;

    /**
     * Creates a new {@code EvaluationContext} with the given attributes.
     *
     * @param portfolioProvider
     *            the {@code PortfolioProvider} from which to obtain {@code Portfolio} information,
     *            or {@code null} if it is known not to be needed
     * @param securityProvider
     *            the {@code SecurityProvider} from which to obtain {@code Security} information, or
     *            {@code null} if it is known not to be needed
     * @param ruleProvider
     *            the {@code RuleProvider} from which to obtain {@code Rule} information, or
     *            {@code null} if it is known not to be needed
     */
    public EvaluationContext(PortfolioProvider portfolioProvider, SecurityProvider securityProvider,
            RuleProvider ruleProvider) {
        this(portfolioProvider, securityProvider, ruleProvider, EvaluationMode.FULL_EVALUATION);
    }

    /**
     * Creates a new {@code EvaluationContext} with the given attributes.
     *
     * @param portfolioProvider
     *            the {@code PortfolioProvider} from which to obtain {@code Portfolio} information,
     *            or {@code null} if it is known not to be needed
     * @param securityProvider
     *            the {@code SecurityProvider} from which to obtain {@code Security} information, or
     *            {@code null} if it is known not to be needed
     * @param ruleProvider
     *            the {@code RuleProvider} from which to obtain {@code Rule} information, or
     *            {@code null} if it is known not to be needed
     * @param evaluationMode
     *            the evaluation mode in which to evaluate
     */
    public EvaluationContext(PortfolioProvider portfolioProvider, SecurityProvider securityProvider,
            RuleProvider ruleProvider, EvaluationMode evaluationMode) {
        this.portfolioProvider = portfolioProvider;
        this.securityProvider = securityProvider;
        this.ruleProvider = ruleProvider;
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

    /**
     * Obtains the {@code PortfolioProvider} to be used by evaluations in this context.
     *
     * @return a {@code PortfolioProvider}
     */
    public PortfolioProvider getPortfolioProvider() {
        return portfolioProvider;
    }

    /**
     * Obtains the {@code RuleProvider} to be used by evaluations in this context.
     *
     * @return a {@code RuleProvider}
     */
    public RuleProvider getRuleProvider() {
        return ruleProvider;
    }

    /**
     * Obtains the {@code SecurityProvider} to be used by evaluations in this context.
     *
     * @return a {@code SecurityProvider}
     */
    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }
}
