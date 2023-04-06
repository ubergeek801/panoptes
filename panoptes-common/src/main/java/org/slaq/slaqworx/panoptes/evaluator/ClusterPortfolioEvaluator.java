package org.slaq.slaqworx.panoptes.evaluator;

import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slaq.slaqworx.panoptes.util.CompletableFutureAdapter;

/**
 * A {@link PortfolioEvaluator} which delegates processing to the cluster.
 *
 * @author jeremy
 */
@Singleton
public class ClusterPortfolioEvaluator implements PortfolioEvaluator {
  @Nonnull private final AssetCache assetCache;

  /**
   * Creates a new {@link ClusterPortfolioEvaluator} using the given {@link AssetCache} for
   * distributed {@link Portfolio} evaluation.
   *
   * @param assetCache the {@link AssetCache} to use to obtain distributed resources
   */
  protected ClusterPortfolioEvaluator(@Nonnull AssetCache assetCache) {
    this.assetCache = assetCache;
  }

  @Override
  @Nonnull
  public CompletableFuture<Map<RuleKey, EvaluationResult>> evaluate(
      @Nonnull PortfolioKey portfolioKey, @Nonnull EvaluationContext evaluationContext) {
    return evaluate(portfolioKey, null, evaluationContext);
  }

  @Override
  @Nonnull
  public CompletableFuture<Map<RuleKey, EvaluationResult>> evaluate(
      @Nonnull PortfolioKey portfolioKey,
      Transaction transaction,
      @Nonnull EvaluationContext evaluationContext) {
    // merely submit a request to the cluster executor
    CompletableFutureAdapter<Map<RuleKey, EvaluationResult>> completableFutureAdapter =
        new CompletableFutureAdapter<>();
    assetCache
        .getClusterExecutor()
        .submit(
            new PortfolioEvaluationRequest(portfolioKey, transaction, evaluationContext),
            completableFutureAdapter);

    return completableFutureAdapter;
  }
}
