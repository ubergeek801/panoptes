package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.tuple.Pair;
import org.slaq.slaqworx.panoptes.rule.BenchmarkComparable;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;

/**
 * A service which compares intermediate results from a base portfolio and a benchmark portfolio to
 * produce a final result.
 *
 * @author jeremy
 */
public class BenchmarkComparator {
  /**
   * Creates a new {@link BenchmarkComparator}.
   */
  public BenchmarkComparator() {
    // nothing to do
  }

  /**
   * Compares the given base portfolio result to the given benchmark result. For the sake of
   * convenience, this method may be invoked on non-benchmark-relative results ({@code
   * benchmarkResult == null} and/or {@code rule.isBenchmarkSupported == false}), in which case the
   * returned result is merely the {@code baseResult}.
   *
   * @param baseResult
   *     the base portfolio result to be compared
   * @param benchmarkResult
   *     the benchmark result to be compared
   * @param rule
   *     the details of the rule controlling the comparison
   *
   * @return an {@link EvaluationResult} encapsulating the individual results and their comparison
   */
  @Nonnull
  public EvaluationResult compare(@Nonnull EvaluationResult baseResult,
      EvaluationResult benchmarkResult, @Nonnull BenchmarkComparable rule) {
    if (benchmarkResult == null) {
      // presume no benchmark used; just use the base result
      return baseResult;
    }

    if (!rule.isBenchmarkSupported()) {
      // no benchmark comparison possible; just use the base result
      return baseResult;
    }

    Double lowerLimit = rule.lowerLimit();
    Double upperLimit = rule.upperLimit();

    Map<EvaluationGroup, ValueResult> baseResults = baseResult.results().entrySet().stream().map(
        e -> Pair.of(e.getKey(),
            compare(e.getValue(), benchmarkResult.results().get(e.getKey()), lowerLimit,
                upperLimit))).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

    Map<EvaluationGroup, ValueResult> proposedResults =
        baseResult.proposedResults().entrySet().stream().map(e -> Pair.of(e.getKey(),
            compare(e.getValue(), benchmarkResult.results().get(e.getKey()), lowerLimit,
                upperLimit))).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

    return new EvaluationResult(baseResult.getRuleKey(), baseResults, proposedResults);
  }

  /**
   * Compares the given base portfolio result to the benchmark result, using (if applicable) the
   * given lower and/or upper limits to determine whether the base result is below, above or within
   * the benchmark-relative limit.
   *
   * @param baseResult
   *     the base portfolio result to be compared
   * @param benchmarkResult
   *     the benchmark result to be compared
   * @param lowerLimit
   *     the lower limit defined by the rule being evaluated, or {@code null} if not applicable
   * @param upperLimit
   *     the upper limit defined by the rule being evaluated, or {@code null} if not applicable
   *
   * @return a {@link ValueResult} indicating the comparison results
   */
  @Nonnull
  protected ValueResult compare(@Nonnull ValueResult baseResult, ValueResult benchmarkResult,
      Double lowerLimit, Double upperLimit) {
    Double benchmarkValue = (benchmarkResult == null ? null : benchmarkResult.getValue());
    // rescale the value to the benchmark; this may result in NaN, which means that the
    // Position's portfolio concentration is infinitely greater than the benchmark
    double scaledValue =
        (benchmarkValue == null ? Double.NaN : baseResult.getValue() / benchmarkValue);

    if (lowerLimit != null && (!Double.isNaN(scaledValue) && scaledValue < lowerLimit)) {
      return new ValueResult(Threshold.BELOW, scaledValue, benchmarkValue);
    }

    if (upperLimit != null && (Double.isNaN(scaledValue) || scaledValue > upperLimit)) {
      return new ValueResult(Threshold.ABOVE, scaledValue, benchmarkValue);
    }

    return new ValueResult(Threshold.WITHIN, scaledValue, benchmarkValue);
  }
}
