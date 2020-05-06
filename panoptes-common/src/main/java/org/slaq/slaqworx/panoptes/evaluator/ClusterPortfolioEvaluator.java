package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slaq.slaqworx.panoptes.util.CompletableFutureAdapter;

/**
 * {@code ClusterPortfolioEvaluator} is a {@code PortfolioEvaluator} which delegates processing to
 * the cluster.
 *
 * @author jeremy
 */
@Singleton
public class ClusterPortfolioEvaluator implements PortfolioEvaluator {
    private final AssetCache assetCache;

    /**
     * Creates a new {@code ClusterPortfolioEvaluator} using the given {@code AssetCache} for
     * distributed {@code Portfolio} evaluation.
     *
     * @param assetCache
     *            the {@code AssetCache} to use to obtain distributed resources
     */
    protected ClusterPortfolioEvaluator(AssetCache assetCache) {
        this.assetCache = assetCache;
    }

    @Override
    public CompletableFuture<Map<RuleKey, EvaluationResult>> evaluate(PortfolioKey portfolioKey,
            EvaluationContext evaluationContext) {
        return evaluate(portfolioKey, null, evaluationContext);
    }

    @Override
    public CompletableFuture<Map<RuleKey, EvaluationResult>> evaluate(PortfolioKey portfolioKey,
            Transaction transaction, EvaluationContext evaluationContext) {
        // merely submit a request to the cluster executor
        CompletableFutureAdapter<Map<RuleKey, EvaluationResult>> completableFutureAdapter =
                new CompletableFutureAdapter<>();
        assetCache.getClusterExecutor().submit(
                new PortfolioEvaluationRequest(portfolioKey, transaction, evaluationContext),
                completableFutureAdapter);

        return completableFutureAdapter;
    }
}
