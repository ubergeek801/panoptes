package org.slaq.slaqworx.panoptes.trade;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.util.ForkJoinPoolFactory;

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

    private static final ForkJoinPool portfolioEvaluationThreadPool = ForkJoinPoolFactory
            .newForkJoinPool(ForkJoinPool.getCommonPoolParallelism(), "portfolio-evaluator");

    private final PortfolioEvaluator evaluator;
    private final PortfolioProvider portfolioProvider;
    private final SecurityProvider securityProvider;

    /**
     * Creates a new {@code TradeEvaluator}.
     *
     * @param evaluator
     *            the {@code PortfolioEvaluator} to use to perform {@code Portfolio}-level
     *            evaluations
     * @param assetCache
     *            the {@code AssetCache} to use to resolve {@code Portfolio} and {@code Security}
     *            references
     */
    public TradeEvaluator(PortfolioEvaluator evaluator, AssetCache assetCache) {
        this(evaluator, assetCache, assetCache);
    }

    /**
     * Creates a new {@code TradeEvaluator}.
     *
     * @param evaluator
     *            the {@code PortfolioEvaluator} to use to perform {@code Portfolio}-level
     *            evaluations
     * @param portfolioProvider
     *            the {@code PortfolioProvider} to use to obtain {@code Portfolio} information
     * @param securityProvider
     *            the {@code SecurityProvider} to use to obtain {@code Security} information
     */
    public TradeEvaluator(PortfolioEvaluator evaluator, PortfolioProvider portfolioProvider,
            SecurityProvider securityProvider) {
        this.evaluator = evaluator;
        this.portfolioProvider = portfolioProvider;
        this.securityProvider = securityProvider;
    }

    /**
     * Evaluates a {@code Trade} against the {@code Portfolio}s impacted by its
     * {@code Transaction}s.
     *
     * @param trade
     *            the {@code Trade} to be evaluated
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to perform the evaluation
     * @return a {@code TradeEvaluationResult} describing the results of the evaluation
     * @throws ExcecutionException
     *             if the calculation could not be processed
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     */
    public TradeEvaluationResult evaluate(Trade trade, EvaluationContext evaluationContext)
            throws ExecutionException, InterruptedException {
        LOG.info("evaluating trade {} with {} allocations", trade.getKey(),
                trade.getAllocationCount());
        // evaluate the impact on each Portfolio
        TradeEvaluationResult evaluationResult = new TradeEvaluationResult();
        portfolioEvaluationThreadPool.submit(() -> trade.getTransactions().entrySet()
                .parallelStream().forEach(portfolioTransactionEntry -> {
                    Portfolio portfolio =
                            portfolioProvider.getPortfolio(portfolioTransactionEntry.getKey());
                    Transaction transaction = portfolioTransactionEntry.getValue();

                    // the impact is merely the difference between the current evaluation state of
                    // the Portfolio, and the state it would have if the Trade were to be posted
                    try {
                        Map<RuleKey, EvaluationResult> ruleResults =
                                evaluator.evaluate(portfolio, transaction, evaluationContext).get();

                        evaluationResult.addImpacts(portfolio.getKey(), ruleResults);
                    } catch (Exception e) {
                        // FIXME throw a better exception
                        throw new RuntimeException("could not evaluate trade", e);
                    }
                })).get();

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
     * @param portfolioKey
     *            the {@code PortfolioKey} identifying the {@code Portfolio} in which to find room
     * @param securityKey
     *            the {@code SecurityKey} identifying the {@code Security} name for which to find
     *            room
     * @param targetValue
     *            the desired investment amount, as USD market value
     * @return the (approximate) maximum market value of the given {@code Security}, less than or
     *         equal to {@code targetValue}, that can be accepted by the {@code Portfolio} without
     *         violating compliance
     * @throws ExcecutionException
     *             if the calculation could not be processed
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     */
    public double evaluateRoom(PortfolioKey portfolioKey, SecurityKey securityKey,
            double targetValue) throws ExecutionException, InterruptedException {
        // first try the minimum allocation to quickly eliminate Portfolios with no room at all

        EvaluationContext evaluationContext = new EvaluationContext(securityProvider,
                portfolioProvider, EvaluationMode.SHORT_CIRCUIT_EVALUATION);

        double minCompliantValue = MIN_ALLOCATION;
        double trialValue = minCompliantValue;
        TradeEvaluationResult evaluationResult =
                testRoom(portfolioKey, securityKey, trialValue, evaluationContext);
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
            evaluationResult = testRoom(portfolioKey, securityKey, trialValue, evaluationContext);
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
     * @param portfolioKey
     *            the {@code PortfolioKey} identifying the {@code Portfolio} in which to find room
     * @param securityKey
     *            the {@code SecurityKey} identifying the {@code Security} name for which to find
     *            room
     * @param targetValue
     *            the desired investment amount, as USD market value
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to perform the evaluation
     * @return a {@code TradeEvaluationResult} indicating the result of the evaluation
     * @throws ExcecutionException
     *             if the calculation could not be processed
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     */
    protected TradeEvaluationResult testRoom(PortfolioKey portfolioKey, SecurityKey securityKey,
            double targetValue, EvaluationContext evaluationContext)
            throws ExecutionException, InterruptedException {
        TaxLot trialAllocation = new TaxLot(targetValue, securityKey);
        Transaction trialTransaction = new Transaction(portfolioKey, List.of(trialAllocation));
        LocalDate tradeDate = LocalDate.now();
        Trade trialTrade = new Trade(tradeDate, tradeDate, Map.of(portfolioKey, trialTransaction));

        return evaluate(trialTrade, evaluationContext);
    }
}
