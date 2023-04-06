package org.slaq.slaqworx.panoptes.pipeline;

import java.io.Serial;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;

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
    extends KeyedCoProcessFunction<
        PortfolioRuleKey, RuleEvaluationResult, RuleEvaluationResult, RuleEvaluationResult> {
  @Serial private static final long serialVersionUID = 1L;

  private static final ValueStateDescriptor<RuleEvaluationResult>
      PORTFOLIO_RESULT_STATE_DESCRIPTOR =
          new ValueStateDescriptor<>("portfolioResult", RuleEvaluationResult.class);
  private static final ValueStateDescriptor<EvaluationResult> BENCHMARK_RESULT_STATE_DESCRIPTOR =
      new ValueStateDescriptor<>("benchmarkResult", EvaluationResult.class);

  // contains the portfolio's rule results mapped by the rule key
  private transient ValueState<RuleEvaluationResult> baseResultState;
  // contains the benchmark's rule results for the keyed benchmark and rule
  private transient ValueState<EvaluationResult> benchmarkResultState;

  /** Creates a new {@link BenchmarkComparator}. */
  public BenchmarkComparator() {
    // nothing to do
  }

  @Override
  public void open(Configuration parameters) {
    baseResultState = getRuntimeContext().getState(PORTFOLIO_RESULT_STATE_DESCRIPTOR);
    benchmarkResultState = getRuntimeContext().getState(BENCHMARK_RESULT_STATE_DESCRIPTOR);
  }

  @Override
  public void processElement1(
      RuleEvaluationResult portfolioResult,
      KeyedCoProcessFunction<
                  PortfolioRuleKey,
                  RuleEvaluationResult,
                  RuleEvaluationResult,
                  RuleEvaluationResult>
              .Context
          context,
      Collector<RuleEvaluationResult> out)
      throws Exception {
    // the element being processed is a portfolio rule evaluation result

    // if the portfolio does not have a benchmark or if the rule does not support benchmarks,
    // then we can pass the result through and forget about it
    PortfolioKey benchmarkKey = portfolioResult.benchmarkKey();
    if (benchmarkKey == null || !portfolioResult.isBenchmarkSupported()) {
      out.collect(portfolioResult);
      return;
    }

    // the portfolio/rule are benchmark-enabled; capture the portfolio state for immediate
    // and/or future publication
    baseResultState.update(portfolioResult);

    // check whether we have the corresponding benchmark results yet
    EvaluationResult benchmarkResult = benchmarkResultState.value();
    if (benchmarkResult == null) {
      // can't do anything yet
      return;
    }

    // benchmark results are present, so proceed with processing
    processPortfolioResult(out, portfolioResult, benchmarkResult);
  }

  @Override
  public void processElement2(
      RuleEvaluationResult benchmarkResult,
      KeyedCoProcessFunction<
                  PortfolioRuleKey,
                  RuleEvaluationResult,
                  RuleEvaluationResult,
                  RuleEvaluationResult>
              .Context
          context,
      Collector<RuleEvaluationResult> out)
      throws Exception {
    // the element being processed is a benchmark rule evaluation result

    // store the benchmark result in the process state
    EvaluationResult benchmarkEvaluationResult = benchmarkResult.evaluationResult();
    benchmarkResultState.update(benchmarkEvaluationResult);

    // check whether we have the corresponding portfolio (base) results yet
    RuleEvaluationResult baseResult = baseResultState.value();
    if (baseResult == null) {
      // can't do anything yet
      return;
    }

    processPortfolioResult(out, baseResult, benchmarkEvaluationResult);
  }

  /**
   * Compares the base portfolio result and corresponding benchmark result and publishes the
   * comparison result to the given collector.
   *
   * @param out the {@link Collector} to which to publish the comparison result
   * @param baseResult the rule evaluation result from the base portfolio
   * @param benchmarkResult the rule evaluation result from the corresponding benchmark
   */
  protected void processPortfolioResult(
      Collector<RuleEvaluationResult> out,
      RuleEvaluationResult baseResult,
      EvaluationResult benchmarkResult) {
    EvaluationResult benchmarkComparisonResult =
        new org.slaq.slaqworx.panoptes.evaluator.BenchmarkComparator()
            .compare(baseResult.evaluationResult(), benchmarkResult, baseResult);

    RuleEvaluationResult finalResult =
        new RuleEvaluationResult(
            baseResult.eventId(),
            baseResult.portfolioKey(),
            baseResult.benchmarkKey(),
            EvaluationSource.BENCHMARK_COMPARISON,
            baseResult.isBenchmarkSupported(),
            baseResult.lowerLimit(),
            baseResult.upperLimit(),
            benchmarkComparisonResult);
    out.collect(finalResult);
  }
}
