package org.slaq.slaqworx.panoptes.trade;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Singleton;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.util.CompletableFutureAdapter;

/**
 * {@code ClusterTradeEvaluator} is a {@code TradeEvaluator} which delegates processing to the
 * cluster.
 *
 * @author jeremy
 */
@Singleton
public class ClusterTradeEvaluator implements TradeEvaluator {
    private final AssetCache assetCache;

    /**
     * Creates a new {@code ClusterTradeEvaluator} using the given {@code AssetCache} for
     * distributed {@code Trade} evaluation.
     *
     * @param assetCache
     *            the {@code AssetCache} to use to obtain distributed resources
     */
    protected ClusterTradeEvaluator(AssetCache assetCache) {
        this.assetCache = assetCache;
    }

    @Override
    public CompletableFuture<TradeEvaluationResult> evaluate(Trade trade,
            EvaluationContext evaluationContext) {
        // merely submit a request to the cluster executor
        CompletableFutureAdapter<TradeEvaluationResult> completableFutureAdapter =
                new CompletableFutureAdapter<>();
        assetCache.getClusterExecutor().submit(
                new TradeEvaluationRequest(trade.getKey(), evaluationContext),
                completableFutureAdapter);

        return completableFutureAdapter;
    }

    @Override
    public CompletableFuture<Double> evaluateRoom(PortfolioKey portfolioKey,
            SecurityKey securityKey, double targetValue)
            throws ExecutionException, InterruptedException {
        // merely submit a request to the cluster executor
        CompletableFutureAdapter<Double> completableFutureAdapter =
                new CompletableFutureAdapter<>();
        assetCache.getClusterExecutor().submit(
                new RoomEvaluationRequest(portfolioKey, securityKey, targetValue),
                completableFutureAdapter);

        return completableFutureAdapter;
    }
}
