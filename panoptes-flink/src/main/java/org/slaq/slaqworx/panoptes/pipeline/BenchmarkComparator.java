package org.slaq.slaqworx.panoptes.pipeline;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

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
public class BenchmarkComparator extends KeyedCoProcessFunction<PortfolioKey, RuleEvaluationResult,
        RuleEvaluationResult, RuleEvaluationResult> {
    private static final long serialVersionUID = 1L;

    private static final MapStateDescriptor<RuleKey,
            RuleEvaluationResult> PORTFOLIO_RESULT_STATE_DESCRIPTOR = new MapStateDescriptor<>(
                    "portfolioResult", RuleKey.class, RuleEvaluationResult.class);
    private static final MapStateDescriptor<RuleKey,
            EvaluationResult> BENCHMARK_RESULT_STATE_DESCRIPTOR = new MapStateDescriptor<>(
                    "benchmarkResult", RuleKey.class, EvaluationResult.class);

    // contains the portfolio's rule results mapped by the rule key
    private transient MapState<RuleKey, RuleEvaluationResult> baseResultsState;
    // contains the benchmark's rule results mapped by the rule key
    private transient MapState<RuleKey, EvaluationResult> benchmarkResultsState;

    /**
     * Creates a new {@code BenchmarkComparator}.
     */
    public BenchmarkComparator() {
        // nothing to do
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        baseResultsState = getRuntimeContext().getMapState(PORTFOLIO_RESULT_STATE_DESCRIPTOR);
        benchmarkResultsState = getRuntimeContext().getMapState(BENCHMARK_RESULT_STATE_DESCRIPTOR);
    }

    @Override
    public void processElement1(RuleEvaluationResult portfolioResult,
            KeyedCoProcessFunction<PortfolioKey, RuleEvaluationResult, RuleEvaluationResult,
                    RuleEvaluationResult>.Context context,
            Collector<RuleEvaluationResult> out) throws Exception {
        // the element being processed is a portfolio rule evaluation result

        // If the portfolio does not have a benchmark or if the rule does not support benchmarks,
        // then we can pass the result through and forget about it. Since this operator expects the
        // stream to be keyed on benchmark ID, and thus it cannot be null, a sentinel value is used
        // in RuleEvaluationResult to indicate no benchmark.
        PortfolioKey benchmarkKey = portfolioResult.getBenchmarkKey();
        if (benchmarkKey == null || benchmarkKey.equals(RuleEvaluationResult.NO_BENCHMARK)
                || !portfolioResult.isBenchmarkSupported()) {
            out.collect(portfolioResult);
            return;
        }

        // the portfolio/rule are benchmark-enabled; capture the portfolio state for immediate
        // and/or future publication
        EvaluationResult baseResult = portfolioResult.getEvaluationResult();
        RuleKey ruleKey = baseResult.getKey();
        baseResultsState.put(ruleKey, portfolioResult);

        // check whether we have the corresponding benchmark results yet
        EvaluationResult benchmarkResult = benchmarkResultsState.get(ruleKey);
        if (benchmarkResult == null) {
            // can't do anything yet
            return;
        }

        // benchmark results are present, so proceed with processing
        processPortfolioResult(out, portfolioResult, benchmarkResult);
    }

    @Override
    public void processElement2(RuleEvaluationResult benchmarkResult,
            KeyedCoProcessFunction<PortfolioKey, RuleEvaluationResult, RuleEvaluationResult,
                    RuleEvaluationResult>.Context context,
            Collector<RuleEvaluationResult> out) throws Exception {
        // the element being processed is a benchmark rule evaluation result

        // store the benchmark result in the process state
        EvaluationResult benchmarkEvaluationResult = benchmarkResult.getEvaluationResult();
        RuleKey ruleKey = benchmarkEvaluationResult.getKey();
        benchmarkResultsState.put(ruleKey, benchmarkEvaluationResult);

        // check whether we have the corresponding portfolio (base) results yet
        RuleEvaluationResult baseResult = baseResultsState.get(ruleKey);
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
     * @param out
     *            the {@code Collector} to which to publish the comparison result
     * @param baseResult
     *            the rule evaluation result from the base portfolio
     * @param benchmarkResult
     *            the rule evaluation result from the corresponding benchmark
     */
    protected void processPortfolioResult(Collector<RuleEvaluationResult> out,
            RuleEvaluationResult baseResult, EvaluationResult benchmarkResult) {
        EvaluationResult benchmarkComparisonResult =
                new org.slaq.slaqworx.panoptes.evaluator.BenchmarkComparator()
                        .compare(baseResult.getEvaluationResult(), benchmarkResult, baseResult);

        RuleEvaluationResult finalResult = new RuleEvaluationResult(baseResult.getEventId(),
                baseResult.getPortfolioKey(), baseResult.getBenchmarkKey(),
                baseResult.isBenchmarkSupported(), baseResult.getLowerLimit(),
                baseResult.getUpperLimit(), benchmarkComparisonResult);
        out.collect(finalResult);
    }
}
