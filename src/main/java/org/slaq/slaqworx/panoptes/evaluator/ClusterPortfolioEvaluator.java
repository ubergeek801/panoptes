package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

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
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.Rule;
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
    public IgniteFuture<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>>
            evaluate(Portfolio portfolio, EvaluationContext evaluationContext)
                    throws InterruptedException, ExecutionException {
        return evaluate(portfolio, null, evaluationContext);
    }

    @Override
    public IgniteFuture<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> evaluate(
            Portfolio portfolio, Transaction transaction, EvaluationContext evaluationContext)
            throws InterruptedException, ExecutionException {
        return evaluate(null, portfolio, transaction, evaluationContext);
    }

    @Override
    public IgniteFuture<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>>
            evaluate(Stream<Rule> rules, Portfolio portfolio, EvaluationContext evaluationContext)
                    throws ExecutionException, InterruptedException {
        return evaluate(rules, portfolio, null, evaluationContext);
    }

    /**
     * Evaluates the given {@code Portfolio} and {@code Transaction} against the given {@code Rule}s
     * (instead of the {@code Portfolio}'s own {@code Rule}s), using the {@code Portfolio}'s
     * associated benchmark (if any).
     *
     * @param rules
     *            the {@code Rule}s to evaluate against the given {@code Portfolio}
     * @param portfolio
     *            the {@code Portfolio} to be evaluated
     * @param transaction
     *            the {@code Transaction} from which to include allocation {@code Position}s for
     *            evaluation
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return a {@code Future} {@code Map} associating each evaluated {@code Rule} with its result
     */
    protected IgniteFuture<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> evaluate(
            Stream<Rule> rules, Portfolio portfolio, Transaction transaction,
            EvaluationContext evaluationContext) {
        long numRules = portfolio.getRules().count();
        if (numRules == 0) {
            LOG.warn("not evaluating Portfolio {} with no Rules", portfolio.getName());
            return new IgniteFinishedFutureImpl<>(Collections.emptyMap());
        }

        if (transaction != null) {
            // the Trade must be available to the cache
            Trade trade = transaction.getTrade();
            assetCache.getTradeCache().put(trade.getKey(), trade);
        }

        try {
            return igniteInstance.compute()
                    .withExecutor(PanoptesCacheConfiguration.REMOTE_PORTFOLIO_EVALUATOR_EXECUTOR)
                    .callAsync(new RemotePortfolioEvaluator(rules, portfolio, transaction,
                            evaluationContext));
        } finally {
            // FIXME remove the cached Trade if we put it there
        }
    }
}
