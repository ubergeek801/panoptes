package org.slaq.slaqworx.panoptes.trade;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * A {@code TradeEvaluator} determines the impact of {@code Trade}s on {@code Portfolio}s by
 * evaluating and comparing the {@code Portfolio}'s current (pre-trade) and proposed (post-trade)
 * states.
 *
 * @author jeremy
 */
public interface TradeEvaluator {
    /**
     * Evaluates a {@code Trade} against the {@code Portfolio}s impacted by its
     * {@code Transaction}s.
     *
     * @param trade
     *            the {@code Trade} to be evaluated
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to perform the evaluation
     * @return a {@code Future} {@code TradeEvaluationResult} describing the results of the
     *         evaluation
     */
    public Future<TradeEvaluationResult> evaluate(Trade trade, EvaluationContext evaluationContext);

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
     * @return a {@Future} {@Double} representing the (approximate) maximum market value of the
     *         given {@code Security}, less than or equal to {@code targetValue}, that can be
     *         accepted by the {@code Portfolio} without violating compliance
     * @throws ExecutionException
     *             if the calculation could not be processed
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     */
    public Future<Double> evaluateRoom(PortfolioKey portfolioKey, SecurityKey securityKey,
            double targetValue) throws ExecutionException, InterruptedException;
}
