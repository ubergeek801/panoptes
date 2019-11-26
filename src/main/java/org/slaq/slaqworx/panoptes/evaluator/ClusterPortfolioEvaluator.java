package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Singleton;

import org.apache.ignite.Ignite;
import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.util.future.IgniteFinishedFutureImpl;
import org.apache.ignite.lang.IgniteFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
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
    private final Affinity<PortfolioKey> affinity;
    private final ClusterNode localNode;

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
        affinity = igniteInstance.affinity(AssetCache.PORTFOLIO_CACHE_NAME);
        localNode = igniteInstance.cluster().localNode();
    }

    @Override
    public IgniteFuture<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            EvaluationContext evaluationContext) throws InterruptedException, ExecutionException {
        return evaluate(portfolio, null, evaluationContext);
    }

    @Override
    public IgniteFuture<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            Transaction transaction, EvaluationContext evaluationContext)
            throws InterruptedException, ExecutionException {
        long numRules = portfolio.getRules().count();
        if (numRules == 0) {
            LOG.warn("not evaluating Portfolio {} with no Rules", portfolio.getKey());
            return new IgniteFinishedFutureImpl<>(Collections.emptyMap());
        }

        // if the Portfolio is local, we can gain some efficiency by skipping the cluster dance
        if (isLocal(portfolio)) {
            return new LocalPortfolioEvaluator(assetCache).evaluate(portfolio, transaction,
                    evaluationContext);
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
        futureResult.listen(result -> {
            LOG.info("received results for Portfolio {} in {} ms", portfolio.getKey(),
                    System.currentTimeMillis() - startTime);
            // remove the temporary Trade from the cache when finished
            if (transaction != null) {
                assetCache.getTradeCache().remove(transaction.getTrade().getKey());
            }
        });

        return futureResult;
    }

    /**
     * Indicates whether the given {@code Portfolio} exists (as primary or backup, if applicable) on
     * the local cache cluster node.
     *
     * @param portfolio
     *            the {@code Portfolio} to be tested
     * @return {@code true} if the given {@code Portfolio} is local, {@code false} otherwise
     */
    protected boolean isLocal(Portfolio portfolio) {
        return false;
        // FIXME enable the real logic when the cache is partitioned
        // return affinity.isPrimaryOrBackup(localNode, portfolio.getKey());
    }
}
