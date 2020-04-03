package org.slaq.slaqworx.panoptes.trade;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Singleton;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

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
    public Future<TradeEvaluationResult> evaluate(Trade trade,
            EvaluationContext evaluationContext) {
        // merely submit a request to the cluster executor
        return assetCache.getClusterExecutor()
                .submit(new TradeEvaluationRequest(trade.getKey(), evaluationContext));
    }

    @Override
    public Future<Double> evaluateRoom(PortfolioKey portfolioKey, SecurityKey securityKey,
            double targetValue) throws ExecutionException, InterruptedException {
        // merely submit a request to the cluster executor
        return assetCache.getClusterExecutor()
                .submit(new RoomEvaluationRequest(portfolioKey, securityKey, targetValue));
    }
}
