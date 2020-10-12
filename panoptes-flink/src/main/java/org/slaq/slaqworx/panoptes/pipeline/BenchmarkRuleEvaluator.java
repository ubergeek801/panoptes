package org.slaq.slaqworx.panoptes.pipeline;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

public class BenchmarkRuleEvaluator extends RuleEvaluator {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(BenchmarkRuleEvaluator.class);

    private static final MapStateDescriptor<RuleKey, Rule> BENCHMARK_RULES_STATE_DESCRIPTOR =
            new MapStateDescriptor<>("benchmarkRules", RuleKey.class, Rule.class);

    private transient MapState<RuleKey, Rule> benchmarkRulesState;

    /**
     * Creates a new {@code BenchmarkRuleEvaluator}.
     */
    public BenchmarkRuleEvaluator() {
        super("benchmark");
    }

    @Override
    public void open(Configuration config) throws Exception {
        super.open(config);
        benchmarkRulesState = getRuntimeContext().getMapState(BENCHMARK_RULES_STATE_DESCRIPTOR);
    }

    @Override
    protected boolean checkPortfolio(Portfolio portfolio) throws Exception {
        // since our input stream is keyed on the portfolio's benchmark, any portfolio that we
        // encounter should have its (benchmark-enabled) rules evaluated against the benchmark
        if (portfolio.getBenchmarkKey() != null) {
            List<Rule> benchmarkEnabledRules = portfolio.getRules()
                    .filter(Rule::isBenchmarkSupported).collect(Collectors.toList());
            if (!benchmarkEnabledRules.isEmpty()) {
                LOG.info("adding {} rules to benchmark {} from portfolio {}",
                        benchmarkEnabledRules.size(), portfolio.getBenchmarkKey(),
                        portfolio.getKey());
                benchmarkEnabledRules.forEach(r -> {
                    try {
                        benchmarkRulesState.put(r.getKey(), r);
                    } catch (Exception e) {
                        // FIXME throw a real exception
                        throw new RuntimeException("could not add rules to process state", e);
                    }
                });
            }
        }

        // only track portfolios that are benchmarks
        return portfolio.isAbstract();
    }

    protected MapState<RuleKey, Rule> getBenchmarkRulesState() {
        return benchmarkRulesState;
    }

    @Override
    protected Iterable<Rule> getEffectiveRules(Portfolio benchmark) throws Exception {
        // use whatever rules we have collected for the benchmark
        return benchmarkRulesState.values();
    }
}
