package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;

/**
 * EvaluationContext provides contextual information that can be shared across multiple evaluations.
 *
 * @author jeremy
 */
public class EvaluationContext {
    private final PortfolioProvider portfolioProvider;
    private final SecurityProvider securityProvider;
    private final RuleProvider ruleProvider;

    /**
     * Creates a new EvaluationContext with the given attributes.
     *
     * @param portfolioProvider
     *            the portfolioProvider from which to obtain Portfolio information, or null if it is
     *            known not to be needed
     * @param securityProvider
     *            the SecurityProvider from which to obtain Security information, or null if it is
     *            known not to be needed
     * @param ruleProvider
     *            the RuleProvider from which to obtain Rule information, or null if it is known not
     *            to be needed
     */
    public EvaluationContext(PortfolioProvider portfolioProvider, SecurityProvider securityProvider,
            RuleProvider ruleProvider) {
        this.portfolioProvider = portfolioProvider;
        this.securityProvider = securityProvider;
        this.ruleProvider = ruleProvider;
    }

    /**
     * Obtains the PortfolioProvider to be used by evaluations in this context.
     *
     * @return a PortfolioProvider
     */
    public PortfolioProvider getPortfolioProvider() {
        return portfolioProvider;
    }

    /**
     * Obtains the RuleProvider to be used by evaluations in this context.
     *
     * @return a RuleProvider
     */
    public RuleProvider getRuleProvider() {
        return ruleProvider;
    }

    /**
     * Obtains the SecurityProvider to be used by evaluations in this context.
     *
     * @return a SecurityProvider
     */
    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }
}
