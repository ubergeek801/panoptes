package org.slaq.slaqworx.panoptes.rule;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.MarketValueProvider;
import org.slaq.slaqworx.panoptes.asset.MarketValued;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttributes;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * Provides contextual information related to the execution of {@link Portfolio} evaluation.
 * Normally, a new {@link EvaluationContext} should be used for each rule evaluation
 * session/instance, but scenarios which would benefit from benchmark value caching across multiple
 * invocations of the same {@link Rule} may reuse an {@link EvaluationContext}.
 *
 * @author jeremy
 */
public class EvaluationContext implements MarketValueProvider, ProtobufSerializable {
  private final SecurityProvider securityProvider;
  private final PortfolioProvider portfolioProvider;
  @Nonnull
  private final EvaluationMode evaluationMode;
  @Nonnull
  private final Map<SecurityKey, SecurityAttributes> securityOverrides;
  private final Map<RuleKey, EvaluationResult> benchmarkResults = new ConcurrentHashMap<>(100);
  private final Map<MarketValued, Double> marketValues =
      Collections.synchronizedMap(new IdentityHashMap<>(10));

  /**
   * Creates a new {@link EvaluationContext} which uses the default {@link AssetCache} to resolve
   * {@link Security} and {@link Portfolio} references, performs full (non-short-circuit) {@link
   * Rule} evaluation, uses the given {@link SecurityProvider} to resolve {@link Security}
   * references, and which supplies no {@link Security} attribute overrides.
   */
  public EvaluationContext() {
    this(null, null, EvaluationMode.FULL_EVALUATION);
  }

  /**
   * Creates a new {@link EvaluationContext} which uses the given evaluation mode, uses the default
   * {@link AssetCache} to resolve references, and which supplies no {@link Security} attribute
   * overrides.
   *
   * @param evaluationMode
   *     the evaluation mode in which to evaluate
   */
  public EvaluationContext(EvaluationMode evaluationMode) {
    this(null, null, evaluationMode, null);
  }

  /**
   * Creates a new {@link EvaluationContext} which uses the given evaluation mode, uses the default
   * {@link AssetCache} to resolve references, and which specifies attributes which should override
   * current {@link Security} attribute values for the purposes of the current evaluation.
   *
   * @param evaluationMode
   *     the evaluation mode in which to evaluate
   * @param securityAttributeOverrides
   *     a (possibly {@code null} or empty) {@link Map} relating a {@link SecurityKey} to a {@link
   *     SecurityAttributes} which should override the current values
   */
  public EvaluationContext(EvaluationMode evaluationMode,
      Map<SecurityKey, SecurityAttributes> securityAttributeOverrides) {
    this(null, null, evaluationMode, securityAttributeOverrides);
  }

  /**
   * Creates a new {@link EvaluationContext} which performs full (non-short-circuit) {@link Rule}
   * evaluation, uses the given {@link SecurityProvider} to resolve {@link Security} references, and
   * which supplies no {@link Security} attribute overrides.
   *
   * @param securityProvider
   *     the {@link SecurityProvider} to use to resolve {@link Security} references
   * @param portfolioProvider
   *     the {@link PortfolioProvider} to use to resolve {@link Portfolio} references
   */
  public EvaluationContext(SecurityProvider securityProvider, PortfolioProvider portfolioProvider) {
    this(securityProvider, portfolioProvider, EvaluationMode.FULL_EVALUATION);
  }

  /**
   * Creates a new {@link EvaluationContext} which uses the given evaluation mode, uses the given
   * providers to resolve references, and which supplies no {@link Security} attribute overrides.
   *
   * @param securityProvider
   *     the {@link SecurityProvider} to use to resolve {@link Security} references
   * @param portfolioProvider
   *     the {@link PortfolioProvider} to use to resolve {@link Portfolio} references
   * @param evaluationMode
   *     the evaluation mode in which to evaluate
   */
  public EvaluationContext(SecurityProvider securityProvider, PortfolioProvider portfolioProvider,
      EvaluationMode evaluationMode) {
    this(securityProvider, portfolioProvider, evaluationMode, null);
  }

  /**
   * Creates a new {@link EvaluationContext} which uses the given evaluation mode, uses the given
   * providers to resolve references, and which specifies attributes which should override current
   * {@link Security} attribute values for the purposes of the current evaluation.
   *
   * @param securityProvider
   *     the {@link SecurityProvider} to use to resolve {@link Security} references
   * @param portfolioProvider
   *     the {@link PortfolioProvider} to use to resolve {@link Portfolio} references
   * @param evaluationMode
   *     the evaluation mode in which to evaluate
   * @param securityAttributeOverrides
   *     a (possibly {@code null} or empty) {@link Map} relating a {@link SecurityKey} to a {@link
   *     SecurityAttributes} which should override the current values
   */
  public EvaluationContext(SecurityProvider securityProvider, PortfolioProvider portfolioProvider,
      @Nonnull EvaluationMode evaluationMode,
      Map<SecurityKey, SecurityAttributes> securityAttributeOverrides) {
    this.securityProvider = securityProvider;
    this.portfolioProvider = portfolioProvider;
    this.evaluationMode = evaluationMode;
    securityOverrides =
        (securityAttributeOverrides == null ? Collections.emptyMap() : securityAttributeOverrides);
  }

  /**
   * Caches the benchmark value for the specified {@link Rule}
   *
   * @param ruleKey
   *     the {@link RuleKey} identifying the currently evaluating {@link Rule}
   * @param result
   *     the benchmark result corresponding to the {@link Rule}
   */
  public void cacheBenchmarkValue(RuleKey ruleKey, EvaluationResult result) {
    benchmarkResults.put(ruleKey, result);
  }

  /**
   * Clears the current context state. Note that it is preferable to use a new {@link
   * EvaluationContext} whenever possible.
   */
  public void clear() {
    benchmarkResults.clear();
    marketValues.clear();
  }

  /**
   * Provides a copy of this {@link EvaluationContext}.
   *
   * @return a new {@link EvaluationContext} copying this one
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
   * Obtains the currently cached benchmark value corresponding to the specified {@link Rule}.
   *
   * @param ruleKey
   *     the {@link RuleKey} identifying the currently evaluating {@link Rule}
   *
   * @return the cached benchmark value if present, or {@code null} otherwise
   */
  public EvaluationResult getBenchmarkResult(RuleKey ruleKey) {
    return benchmarkResults.get(ruleKey);
  }

  /**
   * Obtains the {@link EvaluationMode} in effect for this context.
   *
   * @return a {@link EvaluationMode}
   */
  @Nonnull
  public EvaluationMode getEvaluationMode() {
    return evaluationMode;
  }

  @Override
  public double getMarketValue(@Nonnull MarketValued holding) {
    // compute/cache the market value of this holding
    return marketValues.computeIfAbsent(holding, k -> holding.getMarketValue(this));
  }

  /**
   * Obtains the {@link PortfolioProvider} in effect for the current evaluation.
   *
   * @return a {@link PortfolioProvider}
   */
  @Nonnull
  public PortfolioProvider getPortfolioProvider() {
    return (portfolioProvider != null ? portfolioProvider : AssetCache.getDefault());
  }

  /**
   * Obtains the {@link Security} overrides in effect for the current evaluation.
   *
   * @return a (possibly empty but never {@code null}) {@link Map} relating a {@link SecurityKey} to
   *     a {@link SecurityAttributes} which should override the current values for the purposes of
   *     this evaluation
   */
  @Nonnull
  public Map<SecurityKey, SecurityAttributes> getSecurityOverrides() {
    return securityOverrides;
  }

  /**
   * Obtains the {@link SecurityProvider} in effect for the current evaluation.
   *
   * @return a {@link SecurityProvider}
   */
  @Nonnull
  public SecurityProvider getSecurityProvider() {
    return (securityProvider != null ? securityProvider : AssetCache.getDefault());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + evaluationMode.hashCode();
    result = prime * result + securityOverrides.hashCode();

    return result;
  }

  /**
   * Specifies behaviors to be observed during evaluation.
   */
  public enum EvaluationMode {
    /**
     * all {@link Rule}s are evaluated regardless of other outcomes
     */
    FULL_EVALUATION,
    /**
     * {@link Rule} evaluation may be short-circuited if an evaluation fails
     */
    SHORT_CIRCUIT_EVALUATION
  }
}
