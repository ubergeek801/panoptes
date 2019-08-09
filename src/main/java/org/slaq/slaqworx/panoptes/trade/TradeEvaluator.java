package org.slaq.slaqworx.panoptes.trade;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;

/**
 * A TradeEvaluator determines the impact of Trades on Portfolios by evaluating and comparing the
 * Portfolio's current and proposed states.
 *
 * @author jeremy
 */
public class TradeEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(TradeEvaluator.class);

    private final PortfolioProvider portfolioProvider;
    private final SecurityProvider securityProvider;
    private final RuleProvider ruleProvider;

    /**
     * Creates a new TradeEvaluator.
     *
     * @param portfolioProvider
     *            the PortfolioProvider to use to obtain Portfolio information
     * @param securityProvider
     *            the SecurityProvider to use to obtain Security information
     * @param ruleProvider
     *            the RuleProvider to use to obtain Rule information
     */
    public TradeEvaluator(PortfolioProvider portfolioProvider, SecurityProvider securityProvider,
            RuleProvider ruleProvider) {
        this.portfolioProvider = portfolioProvider;
        this.securityProvider = securityProvider;
        this.ruleProvider = ruleProvider;
    }

    /**
     * Evaluates a Trade against the Portfolios impacted by its Transactions.
     *
     * @param trade
     *            the Trade to be evaluated
     * @return a TradeEvaluationResult describing the results of the evaluation
     * @throws InterruptedException
     *             if the Thread was interrupted while awaiting results
     */
    public TradeEvaluationResult evaluate(Trade trade) throws InterruptedException {
        LOG.info("evaluating trade {} with {} allocations", trade.getId(),
                trade.getAllocationCount());
        // group the Transactions by Portfolio; the result is a Map of Portfolio to the Trade
        // allocations impacting it
        Map<Portfolio, Stream<Position>> portfolioAllocationsMap = trade.getTransactions()
                .collect(Collectors.toMap(t -> t.getPortfolio(), t -> t.getPositions()));

        // evaluate the impact on each Portfolio
        // FIXME use the appropriate ExecutorService
        PortfolioEvaluator evaluator = new PortfolioEvaluator();
        TradeEvaluationResult evaluationResult = new TradeEvaluationResult();
        for (Entry<Portfolio, Stream<Position>> portfolioAllocationEntry : portfolioAllocationsMap
                .entrySet()) {
            Portfolio portfolio = portfolioAllocationEntry.getKey();
            Stream<Position> tradePositions = portfolioAllocationEntry.getValue();

            // the impact is merely the difference between the current evaluation state of the
            // Portfolio, and the state it would have if the Trade were to be posted
            EvaluationContext evaluationContext =
                    new EvaluationContext(portfolioProvider, securityProvider, ruleProvider);
            Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> currentState =
                    evaluator.evaluate(portfolio, evaluationContext);
            Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> proposedState =
                    evaluator.evaluate(portfolio,
                            new PositionSet(Stream.concat(portfolio.getPositions(), tradePositions),
                                    portfolio),
                            evaluationContext);

            proposedState.entrySet().parallelStream().forEach(ruleEntry -> {
                RuleKey ruleKey = ruleEntry.getKey();
                Map<EvaluationGroup<?>, EvaluationResult> proposedGroupResults =
                        ruleEntry.getValue();
                Map<EvaluationGroup<?>, EvaluationResult> currentGroupResults =
                        currentState.get(ruleKey);

                proposedGroupResults.forEach((group, proposedResult) -> {
                    EvaluationResult currentResult = currentGroupResults.get(group);
                    evaluationResult.addImpact(portfolio.getKey(), ruleKey, group,
                            proposedResult.compare(currentResult));
                });
            });
        }

        return evaluationResult;
    }
}
