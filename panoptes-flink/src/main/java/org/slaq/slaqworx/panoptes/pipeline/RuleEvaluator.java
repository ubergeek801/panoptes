package org.slaq.slaqworx.panoptes.pipeline;

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * A process function which collects security and portfolio position data and evaluates portfolio
 * compliance using the portfolio-supplied rules.
 *
 * @author jeremy
 */
public abstract class RuleEvaluator extends KeyedBroadcastProcessFunction<PortfolioKey, Portfolio,
        Security, Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult>> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(RuleEvaluator.class);

    private static final ValueStateDescriptor<Portfolio> PORTFOLIO_STATE_DESCRIPTOR =
            new ValueStateDescriptor<>("portfolio", Portfolio.class);

    private transient ValueState<Portfolio> portfolioState;

    protected RuleEvaluator() {
        // nothing to do
    }

    @Override
    public void open(Configuration config) throws Exception {
        portfolioState = getRuntimeContext().getState(PORTFOLIO_STATE_DESCRIPTOR);
    }

    @Override
    public void processBroadcastElement(Security security,
            KeyedBroadcastProcessFunction<PortfolioKey, Portfolio, Security,
                    Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult>>.Context context,
            Collector<Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult>> out)
            throws Exception {
        BroadcastState<SecurityKey, Security> securityState =
                context.getBroadcastState(PanoptesPipeline.SECURITY_STATE_DESCRIPTOR);
        securityState.put(security.getKey(), security);

        context.applyToKeyedState(PORTFOLIO_STATE_DESCRIPTOR, (portfolioKey,
                state) -> processPortfolio(out, state.value(), security, securityState));
    }

    @Override
    public void processElement(Portfolio portfolio,
            KeyedBroadcastProcessFunction<PortfolioKey, Portfolio, Security,
                    Tuple4<PortfolioKey, PortfolioKey, Rule,
                            EvaluationResult>>.ReadOnlyContext context,
            Collector<Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult>> out)
            throws Exception {
        if (checkPortfolio(portfolio)) {
            getPortfolioState().update(portfolio);
            ReadOnlyBroadcastState<SecurityKey, Security> securityState =
                    context.getBroadcastState(PanoptesPipeline.SECURITY_STATE_DESCRIPTOR);
            processPortfolio(out, portfolio, null, securityState);
        }
    }

    protected abstract boolean checkPortfolio(Portfolio portfolio) throws Exception;

    /**
     * Performs a portfolio evaluation and publishes the result.
     *
     * @param out
     *            the {@code Collector} to which to output compliance results
     * @param portfolio
     *            the portfolio being processed
     * @param securityState
     *            the security information currently held in broadcast state
     * @throws Exception
     *             if an error occurs during processing
     */
    protected void evaluatePortfolio(
            Collector<Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult>> out,
            Portfolio portfolio, ReadOnlyBroadcastState<SecurityKey, Security> securityState)
            throws Exception {
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

        LOG.info("processing portfolio {} (\"{}\")", portfolio.getKey(), portfolio.getName());
        getEffectiveRules(portfolio).forEach(rule -> {
            out.collect(
                    Tuple4.of(portfolio.getKey(), portfolio.getBenchmarkKey(), rule,
                            new org.slaq.slaqworx.panoptes.evaluator.RuleEvaluator(rule, portfolio,
                                    new EvaluationContext(securityProvider, portfolioProvider))
                                            .call()));
        });
    }

    protected abstract Stream<Rule> getEffectiveRules(Portfolio portfolio) throws Exception;

    protected ValueState<Portfolio> getPortfolioState() {
        return portfolioState;
    }

    /**
     * Determines whether the given portfolio is "complete" (all security information has been
     * provided) and performs a compliance evaluation if so.
     *
     * @param out
     *            the {@code Collector} to which to output compliance results
     * @param portfolio
     *            the portfolio being processed
     * @param currentSecurity
     *            the security being encountered, or {@code null} if a portfolio is being
     *            encountered
     * @param securityState
     *            the security information currently held in broadcast state
     * @throws Exception
     *             if an error occurs during processing
     */
    protected void processPortfolio(
            Collector<Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult>> out,
            Portfolio portfolio, Security currentSecurity,
            ReadOnlyBroadcastState<SecurityKey, Security> securityState) throws Exception {
        // determine whether we have all held securities for the portfolio, and whether the current
        // security is in the portfolio
        boolean isComplete = true;
        boolean isCurrentSecurityHeld = (currentSecurity == null);
        Iterator<? extends Position> positionIter = portfolio.getPositions().iterator();
        while (positionIter.hasNext()) {
            Position position = positionIter.next();
            if (!securityState.contains(position.getSecurityKey())) {
                isComplete = false;
                break;
            }
            if (currentSecurity != null
                    && position.getSecurityKey().equals(currentSecurity.getKey())) {
                isCurrentSecurityHeld = true;
            }
        }
        if (!isComplete || !isCurrentSecurityHeld) {
            // we are either not ready or not affected
            return;
        }

        // portfolio is ready for evaluation; proceed
        evaluatePortfolio(out, portfolio, securityState);
    }
}
