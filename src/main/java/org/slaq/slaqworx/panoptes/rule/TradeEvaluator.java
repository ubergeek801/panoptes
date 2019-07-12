package org.slaq.slaqworx.panoptes.rule;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.trade.Trade;

/**
 * A TradeEvaluator determines the impact of Trades on Portfolios by evaluating and comparing the
 * Portfolio's current and proposed states.
 *
 * @author jeremy
 */
public class TradeEvaluator {
    public TradeEvaluator() {
        // nothing to do
    }

    public void evaluate(Trade trade) {
        Map<Portfolio, Stream<Position>> portfolioTradePositionsMap = trade.getTransactions()
                .collect(Collectors.toMap(t -> t.getPortfolio(), t -> t.getPositions()));

        RuleEvaluator evaluator = new RuleEvaluator();

        portfolioTradePositionsMap.forEach((portfolio, tradePositions) -> {
            EvaluationContext evaluationContext = new EvaluationContext();
            Map<Rule, Map<EvaluationGroup<?>, EvaluationResult>> currentState =
                    evaluator.evaluate(portfolio, evaluationContext);
            Map<Rule, Map<EvaluationGroup<?>, EvaluationResult>> proposedState =
                    evaluator.evaluate(portfolio,
                            new PositionSet(Stream.concat(portfolio.getPositions(), tradePositions),
                                    portfolio),
                            evaluationContext);
        });
    }
}
