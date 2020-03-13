package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * {@code ClusterPortfolioEvaluator} is a {@code PortfolioEvaluator} which delegates processing to
 * the cluster.
 *
 * @author jeremy
 */
@Singleton
public class ClusterPortfolioEvaluator implements PortfolioEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(ClusterPortfolioEvaluator.class);

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
    public Future<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            EvaluationContext evaluationContext) {
        return evaluate(portfolio, null, evaluationContext);
    }

    @Override
    public Future<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            Transaction transaction, EvaluationContext evaluationContext) {
        long numRules = portfolio.getRules().count();
        if (numRules == 0) {
            LOG.warn("not evaluating Portfolio {} with no Rules", portfolio.getKey());
            return CompletableFuture.completedFuture(Collections.emptyMap());
        }

        // merely submit a request to the cluster executor
        return assetCache.getClusterExecutor()
                .submit(new PortfolioEvaluationRequest(portfolio, transaction, evaluationContext));
    }
}
