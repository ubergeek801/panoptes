package org.slaq.slaqworx.panoptes.trade;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.PortfolioEvaluator;

/**
 * A TradeEvaluator determines the impact of Trades on Portfolios by evaluating and comparing the
 * Portfolio's current and proposed states.
 *
 * @author jeremy
 */
public class TradeEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(TradeEvaluator.class);

    /**
     * Creates a new TradeEvaluator.
     */
    public TradeEvaluator() {
        // nothing to do
    }

    /**
     * Evaluates a Trade against the Portfolios impacted by its Transactions.
     *
     * @param trade
     *            the Trade to be evaluated
     * @return a TradeEvaluationResult describing the results of the evaluation
     */
    public TradeEvaluationResult evaluate(Trade trade) {
        LOG.info("evaluating trade {} with {} allocations", trade.getId(),
                trade.getAllocationCount());
        // group the Transactions by Portfolio; the result is a Map of Portfolio to the Trade
        // allocations impacting it
        Map<Portfolio, Stream<Position>> portfolioAllocationsMap = trade.getTransactions()
                .collect(Collectors.toMap(t -> t.getPortfolio(), t -> t.getPositions()));

        // evaluate the impact on each Portfolio
        PortfolioEvaluator evaluator = new PortfolioEvaluator();
        TradeEvaluationResult evaluationResult = new TradeEvaluationResult();
        portfolioAllocationsMap.forEach((portfolio, tradePositions) -> {
            // the impact is merely the difference between the current evaluation state of the
            // Portfolio, and the state it would have if the Trade were to be posted
            EvaluationContext evaluationContext = new EvaluationContext();
            Map<Rule, Map<EvaluationGroup<?>, EvaluationResult>> currentState =
                    evaluator.evaluate(portfolio, evaluationContext);
            Map<Rule, Map<EvaluationGroup<?>, EvaluationResult>> proposedState =
                    evaluator.evaluate(portfolio,
                            new PositionSet(Stream.concat(portfolio.getPositions(), tradePositions),
                                    portfolio),
                            evaluationContext);

            proposedState.entrySet().parallelStream().forEach(ruleEntry -> {
                Rule rule = ruleEntry.getKey();
                Map<EvaluationGroup<?>, EvaluationResult> proposedGroupResults =
                        ruleEntry.getValue();
                Map<EvaluationGroup<?>, EvaluationResult> currentGroupResults =
                        currentState.get(rule);

                proposedGroupResults.forEach((group, proposedResult) -> {
                    EvaluationResult currentResult = currentGroupResults.get(group);
                    evaluationResult.addImpact(portfolio, rule, group,
                            proposedResult.compare(currentResult));
                });
            });
        });

        return evaluationResult;
    }
}
