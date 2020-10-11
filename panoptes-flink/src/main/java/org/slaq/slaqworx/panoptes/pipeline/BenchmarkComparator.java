package org.slaq.slaqworx.panoptes.pipeline;

import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.Rule;

public class BenchmarkComparator extends
        KeyedBroadcastProcessFunction<PortfolioKey,
                Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult>,
                Tuple2<PortfolioKey, EvaluationResult>, EvaluationResult> {
    private static final long serialVersionUID = 1L;

    private static final TypeInformation<
            Tuple3<PortfolioRuleKey, Rule, EvaluationResult>> PORTFOLIO_STATE_TYPE_INFO =
                    TypeInformation
                            .of(new TypeHint<Tuple3<PortfolioRuleKey, Rule, EvaluationResult>>() {
                                // trivial
                            });

    private static final ListStateDescriptor<
            Tuple3<PortfolioRuleKey, Rule, EvaluationResult>> PORTFOLIO_RESULT_STATE_DESCRIPTOR =
                    new ListStateDescriptor<>("portfolioResult", PORTFOLIO_STATE_TYPE_INFO);

    private transient ListState<Tuple3<PortfolioRuleKey, Rule, EvaluationResult>> baseResultsState;

    @Override
    public void open(Configuration parameters) throws Exception {
        baseResultsState = getRuntimeContext().getListState(PORTFOLIO_RESULT_STATE_DESCRIPTOR);
    }

    @Override
    public void processBroadcastElement(Tuple2<PortfolioKey, EvaluationResult> benchmarkResult,
            KeyedBroadcastProcessFunction<PortfolioKey,
                    Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult>,
                    Tuple2<PortfolioKey, EvaluationResult>, EvaluationResult>.Context context,
            Collector<EvaluationResult> out) throws Exception {
        // store the benchmark result in the broadcast state
        BroadcastState<PortfolioRuleKey, EvaluationResult> benchmarkResultState =
                context.getBroadcastState(PanoptesPipeline.BENCHMARK_RESULT_STATE_DESCRIPTOR);
        PortfolioKey benchmarkKey = benchmarkResult.f0;
        EvaluationResult benchmarkEvaluationResult = benchmarkResult.f1;
        PortfolioRuleKey benchmarkRuleKey =
                new PortfolioRuleKey(benchmarkKey, benchmarkEvaluationResult.getKey());
        benchmarkResultState.put(benchmarkRuleKey, benchmarkEvaluationResult);

        // any portfolios that use this benchmark can now be computed
        context.applyToKeyedState(PORTFOLIO_RESULT_STATE_DESCRIPTOR, (bk, state) -> {
            state.get().forEach(t -> {
                PortfolioRuleKey portfolioBenchmarkRuleKey = t.f0;
                Rule portfolioRule = t.f1;
                EvaluationResult portfolioResult = t.f2;
                if (portfolioBenchmarkRuleKey.equals(benchmarkRuleKey)) {
                    processPortfolioResult(out, portfolioRule, portfolioResult,
                            benchmarkEvaluationResult);
                }
            });
        });
    }

    @Override
    public void processElement(
            Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult> portfolioResult,
            KeyedBroadcastProcessFunction<PortfolioKey,
                    Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult>,
                    Tuple2<PortfolioKey, EvaluationResult>,
                    EvaluationResult>.ReadOnlyContext context,
            Collector<EvaluationResult> out) throws Exception {
        // if the portfolio does not have a benchmark or if the rule is not benchmark-enabled, then
        // we can pass the result through and forget about it
        PortfolioKey benchmarkKey = portfolioResult.f1;
        Rule rule = portfolioResult.f2;
        EvaluationResult result = portfolioResult.f3;
        if (benchmarkKey == null || !rule.isBenchmarkSupported()) {
            out.collect(result);
            return;
        }

        // the portfolio/rule are benchmark-enabled; capture the portfolio state for immediate
        // and/or future publication
        PortfolioRuleKey benchmarkRuleKey = new PortfolioRuleKey(benchmarkKey, rule.getKey());
        baseResultsState.add(Tuple3.of(benchmarkRuleKey, rule, result));

        ReadOnlyBroadcastState<PortfolioRuleKey, EvaluationResult> benchmarkResultState =
                context.getBroadcastState(PanoptesPipeline.BENCHMARK_RESULT_STATE_DESCRIPTOR);

        EvaluationResult benchmarkResult = benchmarkResultState.get(benchmarkRuleKey);
        if (benchmarkResult == null) {
            // can't do anything yet
            return;
        }

        EvaluationResult baseResult = portfolioResult.f3;
        processPortfolioResult(out, rule, baseResult, benchmarkResult);
    }

    protected void processPortfolioResult(Collector<EvaluationResult> out, Rule rule,
            EvaluationResult baseResult, EvaluationResult benchmarkResult) {
        out.collect(new org.slaq.slaqworx.panoptes.evaluator.BenchmarkComparator()
                .compare(baseResult, benchmarkResult, rule));
    }
}
