package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * The interface for the process of evaluating a set of {@code Rule}s against some {@code Portfolio}
 * and possibly some related benchmark. Processing may be local or distributed based on the
 * implementation.
 *
 * @author jeremy
 */
public interface PortfolioEvaluator {
  /**
   * Evaluates the given {@code Portfolio} using its associated {@code Rule}s and benchmark (if
   * any).
   *
   * @param portfolioKey
   *     the {@code PortfolioKey} identifying the {@code Portfolio} to be evaluated
   * @param evaluationContext
   *     the {@code EvaluationContext} under which to evaluate
   *
   * @return a {@code CompletableFuture} {@code Map} associating each evaluated {@code Rule} with
   *     its result
   */
  CompletableFuture<Map<RuleKey, EvaluationResult>> evaluate(PortfolioKey portfolioKey,
                                                             EvaluationContext evaluationContext);

  /**
   * Evaluates the combined {@code Position}s of the given {@code Portfolio} and {@code
   * Transaction}
   * using the {@code Portfolio} {@code Rule}s and benchmark (if any).
   *
   * @param portfolioKey
   *     the {@code PortfolioKey} identifying the {@code Portfolio} to be evaluated
   * @param transaction
   *     the {@code Transaction} from which to include allocation {@code Position}s for evaluation
   * @param evaluationContext
   *     the {@code EvaluationContext} under which to evaluate
   *
   * @return a {@code CompletableFuture} {@code Map} associating each evaluated {@code Rule} with
   *     its result
   */
  CompletableFuture<Map<RuleKey, EvaluationResult>> evaluate(PortfolioKey portfolioKey,
                                                             Transaction transaction,
                                                             EvaluationContext evaluationContext);
}
