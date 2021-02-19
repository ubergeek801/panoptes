package org.slaq.slaqworx.panoptes.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * A process function which, similarly to {@code PortfolioRuleEvaluator}, collects security and
 * portfolio position data. However, this class evaluates rules only against benchmarks (which are
 * merely portfolios that are specially designated as such). The rules to be evaluated against a
 * particular benchmark are obtained by collecting rules from non-benchmark portfolios which are
 * encountered.
 *
 * @author jeremy
 */
public class BenchmarkRuleEvaluator extends KeyedBroadcastProcessFunction<PortfolioKey,
        PortfolioEvent, Security, RuleEvaluationResult> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(BenchmarkRuleEvaluator.class);

    private static final MapStateDescriptor<RuleKey, Rule> BENCHMARK_RULES_STATE_DESCRIPTOR =
            new MapStateDescriptor<>("benchmarkRules", RuleKey.class, Rule.class);

    private transient PortfolioTracker portfolioTracker;
    private transient MapState<RuleKey, Rule> benchmarkRulesState;

    /**
     * Creates a new {@code BenchmarkRuleEvaluator}.
     */
    public BenchmarkRuleEvaluator() {
        // nothing to do
    }

    @Override
    public void open(Configuration config) throws Exception {
        portfolioTracker = new PortfolioTracker(getRuntimeContext(), EvaluationSource.BENCHMARK);
        benchmarkRulesState = getRuntimeContext().getMapState(BENCHMARK_RULES_STATE_DESCRIPTOR);
    }

    @Override
    public void processBroadcastElement(Security security,
            KeyedBroadcastProcessFunction<PortfolioKey, PortfolioEvent, Security,
                    RuleEvaluationResult>.Context context,
            Collector<RuleEvaluationResult> out) throws Exception {
        portfolioTracker.applySecurity(context, security, (p -> {
            try {
                return benchmarkRulesState.values();
            } catch (Exception e) {
                // FIXME throw a real exception
                throw new RuntimeException("could not get rules for benchmark", e);
            }
        }), out);
    }

    @Override
    public void processElement(PortfolioEvent portfolioEvent,
            KeyedBroadcastProcessFunction<PortfolioKey, PortfolioEvent, Security,
                    RuleEvaluationResult>.ReadOnlyContext context,
            Collector<RuleEvaluationResult> out) throws Exception {
        if (!(portfolioEvent instanceof PortfolioDataEvent)) {
            // not interesting to us
            return;
        }

        Portfolio portfolio = ((PortfolioDataEvent)portfolioEvent).getPortfolio();

        if (portfolio.isAbstract()) {
            portfolioTracker.trackPortfolio(portfolio);
            // the portfolio is a benchmark, so try to process it
            ReadOnlyBroadcastState<SecurityKey, Security> securityState =
                    context.getBroadcastState(PanoptesPipeline.SECURITY_STATE_DESCRIPTOR);
            portfolioTracker.processPortfolio(out, portfolio, null, securityState,
                    benchmarkRulesState.values());
        } else {
            // the portfolio is not a benchmark, but it may have rules that are of interest, so try
            // to extract and process them
            Collection<Rule> newRules = extractRules(portfolio);
            // process any newly-encountered rules against the benchmark
            ReadOnlyBroadcastState<SecurityKey, Security> securityState =
                    context.getBroadcastState(PanoptesPipeline.SECURITY_STATE_DESCRIPTOR);
            portfolioTracker.processPortfolio(out, portfolioTracker.getPortfolio(), null,
                    securityState, newRules);
        }
    }

    /**
     * Extracts benchmark-enabled rules from the given portfolio.
     *
     * @param portfolio
     *            the {@code Portfolio} from which to extract rules
     * @return a {@code Collection} of rules which are benchmark-enabled
     */
    protected Collection<Rule> extractRules(Portfolio portfolio) {
        ArrayList<Rule> newRules = new ArrayList<>();

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
                        if (!benchmarkRulesState.contains(r.getKey())) {
                            // we haven't seen this rule before
                            newRules.add(r);
                        }
                        benchmarkRulesState.put(r.getKey(), r);
                    } catch (Exception e) {
                        // FIXME throw a real exception
                        throw new RuntimeException("could not add rules to process state", e);
                    }
                });
            }
        }

        return newRules;
    }

    /**
     * Obtains the process state containing currently tracked rules. Should only be useful for test
     * cases.
     *
     * @return a {@code MapState} relating rule keys to their corresponding rules
     */
    protected MapState<RuleKey, Rule> getBenchmarkRulesState() {
        return benchmarkRulesState;
    }
}
