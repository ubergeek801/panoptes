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
import org.slaq.slaqworx.panoptes.messaging.ClientProducerSessionFactory;
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
    private final ClientProducerSessionFactory requestProducerSessionFactory;

    /**
     * Creates a new {@code ClusterPortfolioEvaluator} using the given {@code AssetCache} for
     * distributed {@code Portfolio} evaluation.
     *
     * @param assetCache
     *            the {@code AssetCache} to use to obtain distributed resources
     * @param requestProducerSessionFactory
     *            a {@code ClientProducerSessionFactory} providing a {@code ClientSession} and
     *            {@code ClientProducer} corresponding to the {@code Portfolio} evaluation request
     *            queue
     */
    protected ClusterPortfolioEvaluator(AssetCache assetCache,
            ClientProducerSessionFactory requestProducerSessionFactory) {
        this.assetCache = assetCache;
        this.requestProducerSessionFactory = requestProducerSessionFactory;
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

        ClusterEvaluatorDispatcher dispatcher = new ClusterEvaluatorDispatcher(
                assetCache.getPortfolioEvaluationResultMap(), requestProducerSessionFactory,
                portfolio.getKey(), transaction, rules == null ? null : rules.map(r -> r.getKey()));

        // FIXME remove the cached Trade if we put it there

        return dispatcher.getResults();
    }
}
