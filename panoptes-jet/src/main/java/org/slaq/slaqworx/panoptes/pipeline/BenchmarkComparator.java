package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.function.TriFunction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.pipeline.BenchmarkComparator.BenchmarkComparatorState;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg.EvaluationSource;

/**
 * A process function which receives rule evaluation results for portfolios (keyed on the
 * portfolio's <b>benchmark</b> ID) as well as rule evaluation results for benchmarks (keyed on the
 * benchmark's portfolio ID). If a portfolio rule evaluation result is received which does not use a
 * benchmark (either its portfolio does not specify a benchmark, or the rule itself does not support
 * benchmarks), the result is passed through. Otherwise, it is matched against the corresponding
 * result for the same rule against the benchmark, buffering if necessary until the benchmark result
 * arrives.
 *
 * @author jeremy
 */
public class BenchmarkComparator
    implements SupplierEx<BenchmarkComparatorState>, TriFunction<BenchmarkComparatorState,
    PortfolioRuleKey, RuleEvaluationResult, Traverser<RuleEvaluationResult>> {
  static class BenchmarkComparatorState implements Serializable {
    private static final long serialVersionUID = 1L;

    // contains the portfolio's rule results mapped by the rule key
    RuleEvaluationResult baseResult;
    // contains the benchmark's rule results for the keyed benchmark and rule
    EvaluationResult benchmarkResult;
  }

  private static final long serialVersionUID = 1L;

  private transient BenchmarkComparatorState processState;

  /**
   * Creates a new {@code BenchmarkComparator}.
   */
  public BenchmarkComparator() {
    // nothing to do
  }

  @Override
  public Traverser<RuleEvaluationResult> applyEx(BenchmarkComparatorState processState,
                                                 PortfolioRuleKey eventKey,
                                                 RuleEvaluationResult event) {
    this.processState = processState;

    ArrayList<RuleEvaluationResult> results = new ArrayList<>();
    switch (event.getSource()) {
      case BENCHMARK:
        processBenchmarkResult(event, results);
        break;
      case PORTFOLIO:
        processPortfolioResult(event, results);
        break;
      default:
        // FIXME fail somehow
    }

    return Traversers.traverseIterable(results);
  }

  @Override
  public BenchmarkComparatorState getEx() {
    return new BenchmarkComparatorState();
  }

  public void processBenchmarkResult(RuleEvaluationResult portfolioResult,
                                     Collection<RuleEvaluationResult> comparedResults) {
    // the element being processed is a portfolio rule evaluation result

    // if the portfolio does not have a benchmark or if the rule does not support benchmarks,
    // then we can pass the result through and forget about it
    PortfolioKey benchmarkKey = portfolioResult.getBenchmarkKey();
    if (benchmarkKey == null || !portfolioResult.isBenchmarkSupported()) {
      comparedResults.add(portfolioResult);
      return;
    }

    // the portfolio/rule are benchmark-enabled; capture the portfolio state for immediate
    // and/or future publication
    processState.baseResult = portfolioResult;

    // check whether we have the corresponding benchmark results yet
    EvaluationResult benchmarkResult = processState.benchmarkResult;
    if (benchmarkResult == null) {
      // can't do anything yet
      return;
    }

    // benchmark results are present, so proceed with processing
    processPortfolioResult(comparedResults, portfolioResult, benchmarkResult);
  }

  public void processPortfolioResult(RuleEvaluationResult benchmarkResult,
                                     Collection<RuleEvaluationResult> comparedResults) {
    // the element being processed is a benchmark rule evaluation result

    // store the benchmark result in the process state
    EvaluationResult benchmarkEvaluationResult = benchmarkResult.getEvaluationResult();
    processState.benchmarkResult = benchmarkEvaluationResult;

    // check whether we have the corresponding portfolio (base) results yet
    RuleEvaluationResult baseResult = processState.baseResult;
    if (baseResult == null) {
      // can't do anything yet
      return;
    }

    processPortfolioResult(comparedResults, baseResult, benchmarkEvaluationResult);
  }

  /**
   * Compares the base portfolio result and corresponding benchmark result and publishes the
   * comparison result to the given collector.
   *
   * @param comparedResults
   *     the {@code Collection} to which to publish the comparison result
   * @param baseResult
   *     the rule evaluation result from the base portfolio
   * @param benchmarkResult
   *     the rule evaluation result from the corresponding benchmark
   */
  protected void processPortfolioResult(Collection<RuleEvaluationResult> comparedResults,
                                        RuleEvaluationResult baseResult,
                                        EvaluationResult benchmarkResult) {
    EvaluationResult benchmarkComparisonResult =
        new org.slaq.slaqworx.panoptes.evaluator.BenchmarkComparator()
            .compare(baseResult.getEvaluationResult(), benchmarkResult, baseResult);

    RuleEvaluationResult finalResult = new RuleEvaluationResult(baseResult.getEventId(),
        baseResult.getPortfolioKey(), baseResult.getBenchmarkKey(),
        EvaluationSource.BENCHMARK_COMPARISON, baseResult.isBenchmarkSupported(),
        baseResult.getLowerLimit(), baseResult.getUpperLimit(), benchmarkComparisonResult);
    comparedResults.add(finalResult);
  }
}
