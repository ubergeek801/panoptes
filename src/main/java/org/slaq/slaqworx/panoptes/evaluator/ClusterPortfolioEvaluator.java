package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Singleton;

import org.apache.ignite.Ignite;
import org.apache.ignite.internal.util.future.IgniteFinishedFutureImpl;
import org.apache.ignite.lang.IgniteFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.cache.PanoptesCacheConfiguration;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Trade;
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
    private final Ignite igniteInstance;

    /**
     * Creates a new {@code ClusterPortfolioEvaluator} using the given {@code AssetCache} for
     * distributed {@code Portfolio} evaluation.
     *
     * @param assetCache
     *            the {@code AssetCache} to use to obtain distributed resources
     * @param igniteInstance
     *            the {@code Ignite} instance to use for distributed computing
     */
    protected ClusterPortfolioEvaluator(AssetCache assetCache, Ignite igniteInstance) {
        this.assetCache = assetCache;
        this.igniteInstance = igniteInstance;
    }

    @Override
    public IgniteFuture<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            EvaluationContext evaluationContext) throws InterruptedException, ExecutionException {
        return evaluate(portfolio, null, evaluationContext);
    }

    @Override
    public IgniteFuture<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            Transaction transaction, EvaluationContext evaluationContext) {
        long numRules = portfolio.getRules().count();
        if (numRules == 0) {
            LOG.warn("not evaluating Portfolio {} with no Rules", portfolio.getKey());
            return new IgniteFinishedFutureImpl<>(Collections.emptyMap());
        }

        LOG.info("delegating evaluation of Portfolio {}", portfolio.getKey());
        long startTime = System.currentTimeMillis();

        if (transaction != null) {
            // the Trade must be available to the cache
            Trade trade = transaction.getTrade();
            assetCache.getTradeCache().put(trade.getKey(), trade);
        }

        IgniteFuture<Map<RuleKey, EvaluationResult>> futureResult = igniteInstance.compute()
                .withExecutor(PanoptesCacheConfiguration.REMOTE_PORTFOLIO_EVALUATOR_EXECUTOR)
                .callAsync(new RemotePortfolioEvaluator(portfolio, transaction, evaluationContext));
        if (transaction != null) {
            // arrange to remove the temporary Trade from the cache when finished
            futureResult.listen(result -> {
                LOG.info("received results for Portfolio {} in {} ms", portfolio.getKey(),
                        System.currentTimeMillis() - startTime);
                assetCache.getTradeCache().remove(transaction.getTrade().getKey());
            });
        }

        return futureResult;
    }
}
