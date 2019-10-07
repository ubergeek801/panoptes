package org.slaq.slaqworx.panoptes.trade;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code TradeEvaluator} determines the impact of {@code Trade}s on {@code Portfolio}s by
 * evaluating and comparing the {@code Portfolio}'s current (pre-trade) and proposed (post-trade)
 * states.
 *
 * @author jeremy
 */
public class TradeEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(TradeEvaluator.class);

    protected static final double ROOM_TOLERANCE = 500;
    protected static final double MIN_ALLOCATION = 1000;
    private static final double LOG_2 = Math.log(2);

    private final PortfolioEvaluator evaluator;
    private final PortfolioProvider portfolioProvider;

    /**
     * Creates a new {@code TradeEvaluator}.
     *
     * @param evaluator
     *            the {@code PortfolioEvaluator} to use to perform {@code Portfolio}-level
     *            evaluations
     * @param portfolioProvider
     *            the {@code PortfolioProvider} to use to obtain {@code Portfolio} information
     */
    public TradeEvaluator(PortfolioEvaluator evaluator, PortfolioProvider portfolioProvider) {
        this.evaluator = evaluator;
        this.portfolioProvider = portfolioProvider;
    }

    /**
     * Evaluates a {@code Trade} against the {@code Portfolio}s impacted by its
     * {@code Transaction}s.
     *
     * @param trade
     *            the {@code Trade} to be evaluated
     * @param evaluationMode
     *            the {@code TradeEvaluationMode} under which to evaluate the {@code Trade}
     * @return a {@code TradeEvaluationResult} describing the results of the evaluation
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     * @throws ExcecutionException
     *             if the {@code Trade} could not be processed
     */
    public TradeEvaluationResult evaluate(Trade trade, EvaluationMode evaluationMode)
            throws ExecutionException, InterruptedException {
        LOG.info("evaluating trade {} with {} allocations", trade.getKey(),
                trade.getAllocationCount());
        // evaluate the impact on each Portfolio
        TradeEvaluationResult evaluationResult = new TradeEvaluationResult();
        for (Entry<PortfolioKey, Transaction> portfolioTransactionEntry : trade.getTransactions()
                .entrySet()) {
            Portfolio portfolio =
                    portfolioProvider.getPortfolio(portfolioTransactionEntry.getKey());
            Transaction transaction = portfolioTransactionEntry.getValue();

            // the impact is merely the difference between the current evaluation state of the
            // Portfolio, and the state it would have if the Trade were to be posted

            EvaluationContext evaluationContext = new EvaluationContext(evaluationMode);
            Map<RuleKey, EvaluationResult> ruleResults =
                    evaluator.evaluate(portfolio, transaction, evaluationContext).get();

            evaluationResult.addImpacts(portfolio.getKey(), ruleResults);
        }

        return evaluationResult;
    }

    /**
     * Computes the amount of "room" available to accept the specified {@code Security} into the
     * specified {@code Portfolio}.
     * <p>
     * This method uses a binary search algorithm to attempt to converge on an approximate maximum.
     * It will first attempt to allocate {@code MIN_ALLOCATION}, followed by the full target value,
     * after which it will iterate until:
     * <ul>
     * <li>an iteration produces a result within {@code ROOM_TOLERANCE} of the previous result,
     * or</li>
     * <li>the maximum number of iterations (approximately log2({@code targetAmount}) /
     * {@code ROOM_TOLERANCE}) is exceeded.
     * </ul>
     * There is probably a more suitable numerical method; binary search is limited in that:
     * <ul>
     * <li>convergence on an exact solution is slow (hence the cutoff once within
     * {@code ROOM_TOLERANCE};
     * <li>the algorithm is not guaranteed to find a solution even if one exists; and
     * <li>the opportunity to employ parallelism is limited.
     * </ul>
     * FIXME room calculation should take cash (which presumably would be used to acquire the
     * {@code Security}) into account
     *
     * @param portfolio
     *            the {@code Portfolio} in which to find room
     * @param security
     *            the {@code Security} for which to find room
     * @param targetValue
     *            the desired investment amount, as USD market value
     * @return the (approximate) maximum market value of the given {@code Security}, less than or
     *         equal to {@code targetValue}, that can be accepted by the {@code Portfolio} without
     *         violating compliance
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     * @throws ExcecutionException
     *             if the calculation could not be processed
     */
    public double evaluateRoom(Portfolio portfolio, Security security, double targetValue)
            throws InterruptedException, ExecutionException {
        // first try the minimum allocation to quickly eliminate Portfolios with no room at all

        double minCompliantValue = MIN_ALLOCATION;
        double trialValue = minCompliantValue;
        TradeEvaluationResult evaluationResult = testRoom(portfolio, security, trialValue);
        if (!evaluationResult.isCompliant()) {
            // even the minimum allocation failed; give up now
            return 0;
        }

        // we can allocate at least the minimum; try now for the max (target) value and iterate from
        // there

        trialValue = targetValue;
        double minNoncompliantValue = trialValue;
        int maxRoomIterations = (int)Math.ceil(Math.log(targetValue / ROOM_TOLERANCE) / LOG_2) + 1;
        for (int i = 0; i < maxRoomIterations; i++) {
            evaluationResult = testRoom(portfolio, security, trialValue);
            if (evaluationResult.isCompliant()) {
                if (minCompliantValue < trialValue) {
                    // we have a new low-water mark for what is compliant
                    minCompliantValue = trialValue;

                    // now try an amount halfway between the current amount and the lowest amount
                    // known to be noncompliant
                    trialValue = (trialValue + minNoncompliantValue) / 2;
                    // if the new trial value is sufficiently close to the old one, then call it
                    // good
                    if (Math.abs(trialValue - minCompliantValue) < ROOM_TOLERANCE) {
                        return minCompliantValue;
                    }
                }
            } else {
                minNoncompliantValue = trialValue;
                trialValue = (minCompliantValue + trialValue) / 2;
            }
        }

        // number of iterations has expired; this is our final answer
        return minCompliantValue;
    }

    /**
     * Tests for the requested amount of room in the given {@code Security} for the given
     * {@code Portfolio}.
     *
     * @param portfolio
     *            the {@code Portfolio} in which to find room
     * @param security
     *            the {@code Security} for which to find room
     * @param targetValue
     *            the desired investment amount, as USD market value
     * @return a {@code TradeEvaluationResult} indicating the result of the evaluation
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     * @throws ExcecutionException
     *             if the calculation could not be processed
     */
    protected TradeEvaluationResult testRoom(Portfolio portfolio, Security security,
            double targetValue) throws InterruptedException, ExecutionException {
        Position trialAllocation = new Position(targetValue, security);
        Transaction trialTransaction = new Transaction(portfolio, List.of(trialAllocation));
        Trade trialTrade = new Trade(Map.of(portfolio.getKey(), trialTransaction));

        return evaluate(trialTrade, EvaluationMode.SHORT_CIRCUIT_EVALUATION);
    }
}
