package org.slaq.slaqworx.panoptes.event;

import java.util.Objects;
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
 * @author jeremy
 */
public class RuleEvaluationResult implements BenchmarkComparable, ProtobufSerializable {
  private final long eventId;
  private final PortfolioKey portfolioKey;
  private final PortfolioKey benchmarkKey;
  private final EvaluationSource source;
  private final boolean isBenchmarkSupported;
  private final Double lowerLimit;
  private final Double upperLimit;
  private final EvaluationResult evaluationResult;

  /**
   * Creates a new {@link RuleEvaluationResult}.
   *
   * @param eventId
   *     an ID uniquely identifying the result event
   * @param portfolioKey
   *     a key identifying the portfolio/benchmark that this rule was evaluated against
   * @param benchmarkKey
   *     a key identifying the benchmark associated with the evaluated portoflio, or {@code null} if
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
   */
  public RuleEvaluationResult(long eventId, PortfolioKey portfolioKey, PortfolioKey benchmarkKey,
      EvaluationSource source, boolean isBenchmarkSupported, Double lowerLimit, Double upperLimit,
      EvaluationResult evaluationResult) {
    this.eventId = eventId;
    this.portfolioKey = portfolioKey;
    this.benchmarkKey = benchmarkKey;
    this.source = source;
    this.isBenchmarkSupported = isBenchmarkSupported;
    this.lowerLimit = lowerLimit;
    this.upperLimit = upperLimit;
    this.evaluationResult = evaluationResult;
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
    RuleEvaluationResult other = (RuleEvaluationResult) obj;
    return Objects.equals(benchmarkKey, other.benchmarkKey) && eventId == other.eventId &&
        isBenchmarkSupported == other.isBenchmarkSupported &&
        Objects.equals(lowerLimit, other.lowerLimit) &&
        Objects.equals(portfolioKey, other.portfolioKey) && source == other.source &&
        Objects.equals(upperLimit, other.upperLimit);
  }

  public PortfolioRuleKey getBenchmarkEvaluationKey() {
    return new PortfolioRuleKey(benchmarkKey == null ? portfolioKey : benchmarkKey,
        evaluationResult.getRuleKey());
  }

  public PortfolioKey getBenchmarkKey() {
    return benchmarkKey;
  }

  public EvaluationResult getEvaluationResult() {
    return evaluationResult;
  }

  public long getEventId() {
    return eventId;
  }

  @Override
  public Double getLowerLimit() {
    return lowerLimit;
  }

  public PortfolioKey getPortfolioKey() {
    return portfolioKey;
  }

  public EvaluationSource getSource() {
    return source;
  }

  @Override
  public Double getUpperLimit() {
    return upperLimit;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(benchmarkKey, eventId, isBenchmarkSupported, lowerLimit, portfolioKey, source,
            upperLimit);
  }

  @Override
  public boolean isBenchmarkSupported() {
    return isBenchmarkSupported;
  }

  @Override
  public String toString() {
    return "RuleEvaluationResult [eventId=" + eventId + ", portfolioKey=" + portfolioKey +
        ", benchmarkKey=" + benchmarkKey + ", source=" + source + ", isBenchmarkSupported=" +
        isBenchmarkSupported + ", lowerLimit=" + lowerLimit + ", upperLimit=" + upperLimit + "]";
  }
}
