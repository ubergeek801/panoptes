package org.slaq.slaqworx.panoptes.rule;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slaq.slaqworx.panoptes.asset.MarketValueProvider;
import org.slaq.slaqworx.panoptes.asset.MarketValued;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.SecurityAttributes;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * {@code EvaluationContext} provides contextual information related to the execution of
 * {@code Portfolio} evaluation. Normally, a new {@code EvaluationContext} should be used for each
 * {@code RuleEvaluator} instance, but scenarios which would benefit from benchmark value caching
 * across multiple invocations of the same {@code Rule} may reuse an {@code EvaluationContext}.
 *
 * @author jeremy
 */
public class EvaluationContext implements MarketValueProvider, ProtobufSerializable {
    /**
     * {@code EvaluationMode} specifies behaviors to be observed during evaluation.
     */
    public enum EvaluationMode {
        /**
         * all {@code Rule}s are evaluated regardless of outcome
         */
        FULL_EVALUATION,
        /**
         * {@code Rule} evaluation may be short-circuited if an evaluation fails
         */
        SHORT_CIRCUIT_EVALUATION
    }

    private final SecurityProvider securityProvider;
    private final PortfolioProvider portfolioProvider;
    private final EvaluationMode evaluationMode;
    private final Map<SecurityKey, SecurityAttributes> securityOverrides;
    private final Map<RuleKey, EvaluationResult> benchmarkResults = new ConcurrentHashMap<>(100);
    private final Map<MarketValued, Double> marketValues =
            Collections.synchronizedMap(new IdentityHashMap<>(10));

    /**
     * Creates a new {@code EvaluationContext} which performs full (non-short-circuit) {@code Rule}
     * evaluation, uses the given {@code SecurityProvider} to resolve {@code Security} references,
     * and which supplies no {@code Security} attribute overrides.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Security} references
     * @param portfolioProvider
     *            the {@code PortfolioProvider} to use to resolve {@code Portfolio} references
     */
    public EvaluationContext(SecurityProvider securityProvider,
            PortfolioProvider portfolioProvider) {
        this(securityProvider, portfolioProvider, EvaluationMode.FULL_EVALUATION);
    }

    /**
     * Creates a new {@code EvaluationContext} which uses the given evaluation mode, uses the given
     * providers to resolve references, and which supplies no {@code Security} attribute overrides.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Security} references
     * @param portfolioProvider
     *            the {@code PortfolioProvider} to use to resolve {@code Portfolio} references
     * @param evaluationMode
     *            the evaluation mode in which to evaluate
     */
    public EvaluationContext(SecurityProvider securityProvider, PortfolioProvider portfolioProvider,
            EvaluationMode evaluationMode) {
        this(securityProvider, portfolioProvider, evaluationMode, null);
    }

    /**
     * Creates a new {@code EvaluationContext} which uses the given evaluation mode, uses the given
     * providers to resolve references, and which specifies attributes which should override current
     * {@code Security} attribute values for the purposes of the current evaluation.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Security} references
     * @param portfolioProvider
     *            the {@code PortfolioProvider} to use to resolve {@code Portfolio} references
     * @param evaluationMode
     *            the evaluation mode in which to evaluate
     * @param securityAttributeOverrides
     *            a (possibly {@code null} or empty) {@code Map} relating a {@code SecurityKey} to a
     *            {@code SecurityAttributes} which should override the current values
     */
    public EvaluationContext(SecurityProvider securityProvider, PortfolioProvider portfolioProvider,
            EvaluationMode evaluationMode,
            Map<SecurityKey, SecurityAttributes> securityAttributeOverrides) {
        this.securityProvider = securityProvider;
        this.portfolioProvider = portfolioProvider;
        this.evaluationMode = evaluationMode;
        securityOverrides = (securityAttributeOverrides == null ? Collections.emptyMap()
                : securityAttributeOverrides);
    }

    /**
     * Caches the benchmark value for the specified {@code Rule}
     *
     * @param ruleKey
     *            the {@code RuleKey} identifying the currently evaluating {@code Rule}
     * @param result
     *            the benchmark result corresponding to the {@code Rule}
     */
    public void cacheBenchmarkValue(RuleKey ruleKey, EvaluationResult result) {
        benchmarkResults.put(ruleKey, result);
    }

    /**
     * Clears the current context state. Note that it is preferable to use a new
     * {@code EvaluationContext} whenever possible.
     */
    public void clear() {
        benchmarkResults.clear();
        marketValues.clear();
    }

    /**
     * Provides a copy of this {@code EvaluationContext}.
     *
     * @return a new {@code EvaluationContext} copying this one
     */
    public EvaluationContext copy() {
        return new EvaluationContext(securityProvider, portfolioProvider, evaluationMode,
                securityOverrides);
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj);
    }

    /**
     * Obtains the currently cached benchmark value corresponding to the specified {@code Rule}.
     *
     * @param ruleKey
     *            the {@code RuleKey} identifying the currently evaluating {@code Rule}
     * @return the cached benchmark value if present, or {@code null} otherwise
     */
    public EvaluationResult getBenchmarkResult(RuleKey ruleKey) {
        return benchmarkResults.get(ruleKey);
    }

    /**
     * Obtains the {@code EvaluationMode} in effect for this context.
     *
     * @return a {@code EvaluationMode}
     */
    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    @Override
    public double getMarketValue(MarketValued holding) {
        // compute/cache the market value of this holding
        return marketValues.computeIfAbsent(holding, k -> holding.getMarketValue(this));
    }

    /**
     * Obtains the {@code PortfolioProvider} in effect for the current evaluation.
     *
     * @return a {@code PortfolioProvider}
     */
    public PortfolioProvider getPortfolioProvider() {
        return portfolioProvider;
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
