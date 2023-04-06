package org.slaq.slaqworx.panoptes.trade;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * Determines the impact of {@link Trade}s on {@link Portfolio}s by evaluating and comparing the
 * {@link Portfolio}'s current (pre-trade) and proposed (post-trade) states.
 *
 * @author jeremy
 */
public interface TradeEvaluator {
  /**
   * Evaluates a {@link Trade} against the {@link Portfolio}s impacted by its {@link Transaction}s.
   *
   * @param trade the {@link Trade} to be evaluated
   * @param evaluationContext the {@link EvaluationContext} under which to perform the evaluation
   * @return a {@link CompletableFuture} {@link TradeEvaluationResult} describing the results of the
   *     evaluation
   * @throws ExecutionException if the evaluation could not be processed
   * @throws InterruptedException if the {@link Thread} was interrupted during evaluation
   */
  CompletableFuture<TradeEvaluationResult> evaluate(
      Trade trade, EvaluationContext evaluationContext)
      throws ExecutionException, InterruptedException;

  /**
   * Computes the amount of "room" available to accept the specified {@link Security} into the
   * specified {@link Portfolio}.
   *
   * <p>This method uses a binary search algorithm to attempt to converge on an approximate maximum.
   * It will first attempt to allocate {@code MIN_ALLOCATION}, followed by the full target value,
   * after which it will iterate until:
   *
   * <ul>
   *   <li>an iteration produces a result within {@code ROOM_TOLERANCE} of the previous result, or
   *   <li>the maximum number of iterations (approximately log2({@code targetAmount}) / {@code
   *       ROOM_TOLERANCE}) is exceeded.
   * </ul>
   *
   * There is probably a more suitable numerical method; binary search is limited in that:
   *
   * <ul>
   *   <li>convergence on an exact solution is slow (hence the cutoff once within {@code
   *       ROOM_TOLERANCE};
   *   <li>the algorithm is not guaranteed to find a solution even if one exists; and
   *   <li>the opportunity to employ parallelism is limited.
   * </ul>
   *
   * FIXME room calculation should take cash (which presumably would be used to acquire the {@link
   * Security}) into account
   *
   * @param portfolioKey the {@link PortfolioKey} identifying the {@link Portfolio} in which to find
   *     room
   * @param securityKey the {@link SecurityKey} identifying the {@link Security} name for which to
   *     find room
   * @param targetValue the desired investment amount, as USD market value
   * @return a {@link CompletableFuture} {@link Double} representing the (approximate) maximum
   *     market value of of the given {@link Security}, less than or equal to {@code targetValue},
   *     that can be accepted by the {@link Portfolio} without violating compliance
   * @throws ExecutionException if the evaluation could not be processed
   * @throws InterruptedException if the {@link Thread} was interrupted during evaluation
   */
  public CompletableFuture<Double> evaluateRoom(
      PortfolioKey portfolioKey, SecurityKey securityKey, double targetValue)
      throws ExecutionException, InterruptedException;
}
