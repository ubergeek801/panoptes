package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * The interface for the process of evaluating a set of {@link Rule}s against some {@link Portfolio}
 * and possibly some related benchmark. Processing may be local or distributed based on the
 * implementation.
 *
 * @author jeremy
 */
public interface PortfolioEvaluator {
  /**
   * Evaluates the given {@link Portfolio} using its associated {@link Rule}s and benchmark (if
   * any).
   *
   * @param portfolioKey the {@link PortfolioKey} identifying the {@link Portfolio} to be evaluated
   * @param evaluationContext the {@link EvaluationContext} under which to evaluate
   * @return a {@link CompletableFuture} {@link Map} associating each evaluated {@link Rule} with
   *     its result
   */
  @Nonnull
  CompletableFuture<Map<RuleKey, EvaluationResult>> evaluate(
      @Nonnull PortfolioKey portfolioKey, @Nonnull EvaluationContext evaluationContext);

  /**
   * Evaluates the combined {@link Position}s of the given {@link Portfolio} and {@link Transaction}
   * using the {@link Portfolio} {@link Rule}s and benchmark (if any).
   *
   * @param portfolioKey the {@link PortfolioKey} identifying the {@link Portfolio} to be evaluated
   * @param transaction the {@link Transaction} from which to include allocation {@link Position}s
   *     for evaluation
   * @param evaluationContext the {@link EvaluationContext} under which to evaluate
   * @return a {@link CompletableFuture} {@link Map} associating each evaluated {@link Rule} with
   *     its result
   */
  @Nonnull
  CompletableFuture<Map<RuleKey, EvaluationResult>> evaluate(
      @Nonnull PortfolioKey portfolioKey,
      Transaction transaction,
      @Nonnull EvaluationContext evaluationContext);
}
