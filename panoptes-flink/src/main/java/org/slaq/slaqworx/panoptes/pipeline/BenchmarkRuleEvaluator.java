package org.slaq.slaqworx.panoptes.pipeline;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.rule.Rule;

public class BenchmarkRuleEvaluator extends RuleEvaluator {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(BenchmarkRuleEvaluator.class);

    private static final ListStateDescriptor<Rule> BENCHMARK_RULES_STATE_DESCRIPTOR =
            new ListStateDescriptor<>("benchmarkRules", Rule.class);

    private transient ListState<Rule> benchmarkRulesState;

    public BenchmarkRuleEvaluator() {
        // nothing to do
    }

    @Override
    public void open(Configuration config) throws Exception {
        super.open(config);
        benchmarkRulesState = getRuntimeContext().getListState(BENCHMARK_RULES_STATE_DESCRIPTOR);
    }

    @Override
    protected boolean checkPortfolio(Portfolio portfolio) throws Exception {
        // since our input stream is keyed on the portfolio's benchmark, any portfolio that we
        // encounter should have its (benchmark-enabled) rules evaluated against the benchmark
        List<Rule> benchmarkEnabledRules = portfolio.getRules().filter(Rule::isBenchmarkSupported)
                .collect(Collectors.toList());
        if (!benchmarkEnabledRules.isEmpty()) {
            LOG.info("adding {} rules to benchmark {} from portfolio {}",
                    benchmarkEnabledRules.size(), portfolio.getBenchmarkKey(), portfolio.getKey());
            benchmarkRulesState.addAll(benchmarkEnabledRules);
        }

        // only track portfolios that are benchmarks
        return portfolio.isAbstract();
    }

    @Override
    protected Stream<Rule> getEffectiveRules(Portfolio benchmark) throws Exception {
        // use whatever rules we have collected for the benchmark
        return StreamSupport.stream(benchmarkRulesState.get().spliterator(), false);
    }
}
