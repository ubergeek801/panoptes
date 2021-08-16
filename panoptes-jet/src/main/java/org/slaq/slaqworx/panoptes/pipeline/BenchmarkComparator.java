package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.function.TriFunction;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.pipeline.BenchmarkComparator.BenchmarkComparatorState;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;

/**
 * A transformation which receives rule evaluation results for portfolios (keyed on the portfolio's
 * <b>benchmark</b> ID + rule key) as well as rule evaluation results for benchmarks (keyed on the
 * benchmark's portfolio ID + rule key). If a portfolio rule evaluation result is received which
 * does not use a benchmark (either its portfolio does not specify a benchmark, or the rule itself
 * does not support benchmarks), the result is passed through. Otherwise, it is matched against the
 * corresponding result for the same rule against the benchmark, buffering if necessary until the
 * benchmark result arrives.
 * <p>
 * This operator affects the following metrics:
 * <ul>
 * <li>increments the {@code benchmarkRuleResults} counter for each benchmark rule result that is
 * encountered</li>;
 * <li>increments the {@code portfolioRuleResults} counter for each portfolio rule result that is
 * encountered; and</li>
 * <li>increments the {@code benchmarkComparisonRuleResults} counter for each individual rule
 * compared against a benchmark</li>
 * </ul>
 *
 * @author jeremy
 */
public class BenchmarkComparator implements SupplierEx<BenchmarkComparatorState>,
    TriFunction<BenchmarkComparatorState, PortfolioRuleKey, RuleEvaluationResult,
        Traverser<RuleEvaluationResult>> {
  @Serial
  private static final long serialVersionUID = 1L;

  private transient BenchmarkComparatorState processState;

  // FIXME provide this at state creation time somehow
  private static MeterRegistry meterRegistry;

  /**
   * Provides a key extraction function suitable for partitioning the input to this transformation.
   *
   * @return a key extractor
   */
  @Nonnull
  public static FunctionEx<RuleEvaluationResult, PortfolioRuleKey> keyExtractor() {
    return RuleEvaluationResult::getBenchmarkEvaluationKey;
  }

  /**
   * Creates a new {@link BenchmarkComparator} which publishes metrics to the given {@link
   * MeterRegistry}.
   *
   * @param meterRegistry
   *     the {@link MeterRegistry} to which to publish metrics
   */
  public BenchmarkComparator(MeterRegistry meterRegistry) {
    BenchmarkComparator.meterRegistry = meterRegistry;
  }

  @Override
  @Nonnull
  public Traverser<RuleEvaluationResult> applyEx(BenchmarkComparatorState processState,
      PortfolioRuleKey eventKey, RuleEvaluationResult event) {
    this.processState = processState;

    ArrayList<RuleEvaluationResult> results = new ArrayList<>();
    switch (event.source()) {
    case BENCHMARK:
      handleBenchmarkResultEvent(event, results);
      break;
    case PORTFOLIO:
      handlePortfolioResultEvent(event, results);
      break;
    default:
      // FIXME fail somehow
    }

    return Traversers.traverseIterable(results);
  }

  @Override
  @Nonnull
  public BenchmarkComparatorState getEx() {
    return new BenchmarkComparatorState();
  }

  /**
   * Handles a {@link RuleEvaluationResult} event for a benchmark; if any results for corresponding
   * portfolios exist, they are compared against the benchmark results, and final results are
   * determined.
   *
   * @param benchmarkResult
   *     a {@link RuleEvaluationResult} from a benchmark
   * @param comparedResults
   *     a {@link Collection} into which final results, if any, are added
   */
  protected void handleBenchmarkResultEvent(@Nonnull RuleEvaluationResult benchmarkResult,
      @Nonnull Collection<RuleEvaluationResult> comparedResults) {
    meterRegistry.counter("benchmarkRuleResults").increment();

    // store the benchmark result in the process state
    EvaluationResult benchmarkEvaluationResult = benchmarkResult.evaluationResult();
    processState.benchmarkResult = benchmarkEvaluationResult;

    // check whether we have the corresponding portfolio (base) results yet
    RuleEvaluationResult baseResult = processState.baseResult;
    if (baseResult == null) {
      // can't do anything yet
      return;
    }

    compareResults(comparedResults, baseResult, benchmarkEvaluationResult);
  }

  /**
   * Handles a {@link RuleEvaluationResult} event for a portfolio; if any results for the
   * corresponding benchmark exist, they are compared against the portfolio results, and final
   * results are determined. Results for portfolios which are not benchmark-relative are passed
   * through.
   *
   * @param portfolioResult
   *     a {@link RuleEvaluationResult} from a portfolio
   * @param comparedResults
   *     a {@link Collection} into which final results, if any, are added
   */
  protected void handlePortfolioResultEvent(@Nonnull RuleEvaluationResult portfolioResult,
      @Nonnull Collection<RuleEvaluationResult> comparedResults) {
    meterRegistry.counter("portfolioRuleResults").increment();

    // if the portfolio does not have a benchmark or if the rule does not support benchmarks, then
    // we can pass the result through and forget about it
    PortfolioKey benchmarkKey = portfolioResult.benchmarkKey();
    if (benchmarkKey == null || !portfolioResult.isBenchmarkSupported()) {
      comparedResults.add(portfolioResult);
      return;
    }

    // the portfolio/rule are benchmark-enabled; capture the portfolio state for immediate and/or
    // future publication
    processState.baseResult = portfolioResult;

    // check whether we have the corresponding benchmark results yet
    EvaluationResult benchmarkResult = processState.benchmarkResult;
    if (benchmarkResult == null) {
      // can't do anything yet
      return;
    }

    // benchmark results are present, so proceed with processing
    compareResults(comparedResults, portfolioResult, benchmarkResult);
  }

  /**
   * Compares the base portfolio result and corresponding benchmark result and adds the comparison
   * result to the given collection.
   *
   * @param comparedResults
   *     the {@link Collection} to which to add the comparison result
   * @param baseResult
   *     the rule evaluation result from the base portfolio
   * @param benchmarkResult
   *     the rule evaluation result from the corresponding benchmark
   */
  protected void compareResults(@Nonnull Collection<RuleEvaluationResult> comparedResults,
      @Nonnull RuleEvaluationResult baseResult, @Nonnull EvaluationResult benchmarkResult) {
    EvaluationResult benchmarkComparisonResult =
        new org.slaq.slaqworx.panoptes.evaluator.BenchmarkComparator().compare(
            baseResult.evaluationResult(), benchmarkResult, baseResult);

    RuleEvaluationResult finalResult =
        new RuleEvaluationResult(baseResult.eventId(), baseResult.portfolioKey(),
            baseResult.benchmarkKey(), EvaluationSource.BENCHMARK_COMPARISON,
            baseResult.isBenchmarkSupported(), baseResult.lowerLimit(), baseResult.upperLimit(),
            benchmarkComparisonResult);
    comparedResults.add(finalResult);

    meterRegistry.counter("benchmarkComparisonRuleResults").increment();
  }

  /**
   * Contains the benchmark comparison process state, which comprises, for a given rule key and
   * benchmark ID key, the most recent portfolio rule result encountered (if any) as well as the
   * most recent benchmark rule result encountered (if any).
   */
  static class BenchmarkComparatorState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // contains the portfolio's rule results mapped by the rule key
    RuleEvaluationResult baseResult;
    // contains the benchmark's rule results for the keyed benchmark and rule
    EvaluationResult benchmarkResult;
  }
}
