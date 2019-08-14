package org.slaq.slaqworx.panoptes.trade;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.evaluator.LocalPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;

/**
 * {@code TradeEvaluator} determines the impact of {@code Trade}s on {@code Portfolio}s by
 * evaluating and comparing the {@code Portfolio}'s current (pre-trade) and proposed (post-trade)
 * states.
 *
 * @author jeremy
 */
public class TradeEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(TradeEvaluator.class);

    private final PortfolioProvider portfolioProvider;
    private final SecurityProvider securityProvider;
    private final RuleProvider ruleProvider;

    /**
     * Creates a new {@code TradeEvaluator}.
     *
     * @param portfolioProvider
     *            the {@code PortfolioProvider} to use to obtain {@code Portfolio} information
     * @param securityProvider
     *            the {@code SecurityProvider} to use to obtain {@code Security} information
     * @param ruleProvider
     *            the {@code RuleProvider} to use to obtain {@code Rule} information
     */
    public TradeEvaluator(PortfolioProvider portfolioProvider, SecurityProvider securityProvider,
            RuleProvider ruleProvider) {
        this.portfolioProvider = portfolioProvider;
        this.securityProvider = securityProvider;
        this.ruleProvider = ruleProvider;
    }

    /**
     * Evaluates a {@code Trade} against the {@code Portfolio}s impacted by its
     * {@code Transaction}s.
     *
     * @param trade
     *            the {@code Trade} to be evaluated
     * @return a {@code TradeEvaluationResult} describing the results of the evaluation
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     * @throws ExcecutionException
     *             if the {@code Trade} could not be processed
     */
    public TradeEvaluationResult evaluate(Trade trade)
            throws ExecutionException, InterruptedException {
        LOG.info("evaluating trade {} with {} allocations", trade.getId(),
                trade.getAllocationCount());
        // group the Transactions by Portfolio; the result is a Map of Portfolio to the Trade
        // allocations impacting it
        Map<Portfolio, Stream<Position>> portfolioAllocationsMap = trade.getTransactions()
                .collect(Collectors.toMap(t -> t.getPortfolio(), t -> t.getPositions()));

        // evaluate the impact on each Portfolio
        // FIXME use the appropriate ExecutorService
        LocalPortfolioEvaluator evaluator = new LocalPortfolioEvaluator();
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
                    evaluator.evaluate(portfolio, evaluationContext).get();
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
