package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
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
    public Future<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>>
            evaluate(Portfolio portfolio, EvaluationContext evaluationContext)
                    throws InterruptedException, ExecutionException {
        return evaluate(portfolio, null, evaluationContext);
    }

    @Override
    public Future<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> evaluate(
            Portfolio portfolio, Transaction transaction, EvaluationContext evaluationContext)
            throws InterruptedException, ExecutionException {
        return evaluate(null, portfolio, transaction, evaluationContext);
    }

    @Override
    public Future<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>>
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
    protected Future<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> evaluate(
            Stream<Rule> rules, Portfolio portfolio, Transaction transaction,
            EvaluationContext evaluationContext) {
        long numRules = portfolio.getRules().count();
        if (numRules == 0) {
            LOG.warn("not evaluating Portfolio {} with no Rules", portfolio.getName());
            return CompletableFuture.completedFuture(Collections.emptyMap());
        }

        if (transaction != null) {
            // the Trade must be available to the cache
            Trade trade = transaction.getTrade();
            assetCache.getTradeCache().putTransient(trade.getKey(), trade, 10, TimeUnit.MINUTES);
        }

        ClusterEvaluatorDispatcher dispatcher =
                new ClusterEvaluatorDispatcher(assetCache.getPortfolioEvaluationRequestQueue(),
                        assetCache.getPortfolioEvaluationResultMap(), portfolio.getKey(),
                        transaction, rules == null ? null : rules.map(r -> r.getKey()));

        // FIXME remove the cached Trade if we put it there

        return dispatcher.getResults();
    }
}
