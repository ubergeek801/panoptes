package org.slaq.slaqworx.panoptes.pipeline;

import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.RuleEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * A process function which collects security and portfolio position data and evaluates portfolio
 * compliance using the portfolio-supplied rules.
 *
 * @author jeremy
 */
public class PortfolioRuleEvaluator
        extends KeyedBroadcastProcessFunction<PortfolioKey, Portfolio, Security, EvaluationResult> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(PortfolioRuleEvaluator.class);

    private static final ValueStateDescriptor<PortfolioState> PORTFOLIO_STATE_DESCRIPTOR =
            new ValueStateDescriptor<>("portfolio", PortfolioState.class);

    private transient ValueState<PortfolioState> portfolioState;

    @Override
    public void open(Configuration config) throws Exception {
        portfolioState = getRuntimeContext().getState(PORTFOLIO_STATE_DESCRIPTOR);
    }

    @Override
    public void processBroadcastElement(Security security,
            KeyedBroadcastProcessFunction<PortfolioKey, Portfolio, Security,
                    EvaluationResult>.Context context,
            Collector<EvaluationResult> out) throws Exception {
        BroadcastState<SecurityKey, Security> securityState =
                context.getBroadcastState(PanoptesPipeline.SECURITY_STATE_DESCRIPTOR);
        securityState.put(security.getKey(), security);

        context.applyToKeyedState(PORTFOLIO_STATE_DESCRIPTOR,
                (portfolioKey, state) -> processPortfolio(out, state.value(), securityState));
    }

    @Override
    public void processElement(Portfolio portfolio,
            KeyedBroadcastProcessFunction<PortfolioKey, Portfolio, Security,
                    EvaluationResult>.ReadOnlyContext context,
            Collector<EvaluationResult> out) throws Exception {
        LOG.info("processing portfolio {} (\"{}\")", portfolio.getKey(), portfolio.getName());
        portfolioState.update(new PortfolioState(portfolio));
        ReadOnlyBroadcastState<SecurityKey, Security> securityState =
                context.getBroadcastState(PanoptesPipeline.SECURITY_STATE_DESCRIPTOR);

        processPortfolio(out, portfolioState.value(), securityState);
    }

    /**
     * Performs a portfolio evaluation and publishes the result.
     *
     * @param out
     *            the {@code Collector} to which to output compliance results
     * @param portfolioState
     *            the state of the portfolio being processed
     * @param securityState
     *            the security information currently held in broadcast state
     */
    protected void evaluatePortfolio(Collector<EvaluationResult> out, PortfolioState portfolioState,
            ReadOnlyBroadcastState<SecurityKey, Security> securityState) {
        Portfolio portfolio = portfolioState.getPortfolio();

        portfolioState.setPublished(true);

        // this is questionable but there shouldn't be any other portfolios queried
        PortfolioProvider portfolioProvider = (k -> portfolio);
        SecurityProvider securityProvider = (k, context) -> {
            try {
                return securityState.get(k);
            } catch (Exception e) {
                // FIXME throw a real exception
                throw new RuntimeException("could not get security " + k, e);
            }
        };

        // FIXME deal with benchmark
        portfolio.getRules().forEach(rule -> out.collect(new RuleEvaluator(rule, portfolio,
                new EvaluationContext(securityProvider, portfolioProvider)).call()));
    }

    /**
     * Determines whether the given portfolio is "complete" (all security information has been
     * provided) and performs a compliance evaluation if so.
     *
     * @param out
     *            the {@code Collector} to which to output compliance results
     * @param portfolioState
     *            the state of the portfolio being processed
     * @param securityState
     *            the security information currently held in broadcast state
     */
    protected void processPortfolio(Collector<EvaluationResult> out, PortfolioState portfolioState,
            ReadOnlyBroadcastState<SecurityKey, Security> securityState) {
        if (portfolioState.isPublished()) {
            // never mind
            return;
        }

        Portfolio portfolio = portfolioState.getPortfolio();
        // determine whether we have all held securities for the portfolio
        boolean isComplete = portfolio.getPositions().allMatch(p -> {
            try {
                return securityState.contains(p.getSecurityKey());
            } catch (Exception e) {
                // FIXME throw a real exception
                throw new RuntimeException(
                        "could not determine completeness for " + portfolio.getKey(), e);
            }
        });
        if (!isComplete) {
            return;
        }

        // portfolio is ready for evaluation; proceed
        evaluatePortfolio(out, portfolioState, securityState);
    }
}
