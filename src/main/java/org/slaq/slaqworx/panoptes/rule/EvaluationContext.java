package org.slaq.slaqworx.panoptes.rule;

import java.util.concurrent.ConcurrentHashMap;

import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;

/**
 * EvaluationContext provides contextual information that can be shared across multiple evaluations.
 * Currently it is used experimentally to cache benchmark evaluation results between the "current"
 * and "proposed" phases of a Trade evaluation.
 *
 * @author jeremy
 */
public class EvaluationContext {
    private final ConcurrentHashMap<Rule, Double> previousBenchmarkValues =
            new ConcurrentHashMap<>(1000);

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
     * Obtains the benchmark value previously calculated for the given Rule.
     *
     * @param rule
     *            the Rule for which to obtain the previous benchmark value
     * @return the previously-computed benchmark value for the given Rule, or null if none was
     *         computed
     */
    public Double getPreviousBenchmarkValue(Rule rule) {
        return previousBenchmarkValues.get(rule);
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

    /**
     * Specifies the benchmark value calculated for the given Rule.
     *
     * @param rule
     *            the rule for which the benchmark value was calculated
     * @param value
     *            the value computed from the benchmark
     */
    public void setPreviousBenchmarkValue(Rule rule, double value) {
        previousBenchmarkValues.put(rule, value);
    }
}
