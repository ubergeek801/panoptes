package org.slaq.slaqworx.panoptes.event;

import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.BenchmarkComparable;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * Encapsulates the result of evaluating a rule against a portfolio or benchmark, or the result of
 * comparing a portfolio result to a benchmark result.
 *
 * @param eventId
 *     an ID uniquely identifying the result event
 * @param portfolioKey
 *     a key identifying the portfolio/benchmark that this rule was evaluated against
 * @param benchmarkKey
 *     a key identifying the benchmark associated with the evaluated portfolio, or {@code null} if
 *     the portfolio has no benchmark (or is itself a benchmark)
 * @param source
 *     the type of entity which was the source of the evaluation result
 * @param isBenchmarkSupported
 *     {@code true} if the evaluated rule supports benchmarks (whether or not the evaluated
 *     portfolio actually has one)
 * @param lowerLimit
 *     the lower limit of the rule, or {@code null} if not applicable
 * @param upperLimit
 *     the upper limit of the rule, or {@code null} if not applicable
 * @param evaluationResult
 *     the result of evaluating the rule against the portfolio/benchmark
 *
 * @author jeremy
 */
public record RuleEvaluationResult(long eventId, @Nonnull PortfolioKey portfolioKey,
                                   PortfolioKey benchmarkKey, @Nonnull EvaluationSource source,
                                   boolean isBenchmarkSupported, Double lowerLimit,
                                   Double upperLimit, @Nonnull EvaluationResult evaluationResult)
    implements BenchmarkComparable, ProtobufSerializable {
  @Nonnull
  public PortfolioRuleKey getBenchmarkEvaluationKey() {
    return new PortfolioRuleKey(benchmarkKey == null ? portfolioKey : benchmarkKey,
        evaluationResult.getRuleKey());
  }

  @Override
  public Double getLowerLimit() {
    return lowerLimit;
  }

  @Override
  public Double getUpperLimit() {
    return upperLimit;
  }

  @Override
  @Nonnull
  public String toString() {
    return "RuleEvaluationResult [eventId=" + eventId + ", portfolioKey=" + portfolioKey +
        ", benchmarkKey=" + benchmarkKey + ", source=" + source + ", isBenchmarkSupported=" +
        isBenchmarkSupported + ", lowerLimit=" + lowerLimit + ", upperLimit=" + upperLimit + "]";
  }
}
