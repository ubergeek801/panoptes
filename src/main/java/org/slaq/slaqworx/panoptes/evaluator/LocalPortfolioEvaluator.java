package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ignite.internal.util.future.IgniteFinishedFutureImpl;
import org.apache.ignite.lang.IgniteFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slaq.slaqworx.panoptes.util.ForkJoinPoolFactory;

/**
 * {@code LocalPortfolioEvaluator} is a {@code PortfolioEvaluator} which performs processing on the
 * local node. This is where parallelization of {@code Rule} processing occurs.
 *
 * @author jeremy
 */
public class LocalPortfolioEvaluator implements PortfolioEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(LocalPortfolioEvaluator.class);

    private static final ForkJoinPool ruleEvaluationThreadPool = ForkJoinPoolFactory
            .newForkJoinPool(ForkJoinPool.getCommonPoolParallelism(), "rule-evaluator");

    private final PortfolioProvider portfolioProvider;

    /**
     * Creates a new {@code LocalPortfolioEvaluator} that uses a {@code ForkJoinPool} for local
     * {@code Portfolio} evaluation.
     *
     * @param portfolioProvider
     *            the {@code PortfolioProvider} to use to resolve {@code Portfolio} references
     */
    public LocalPortfolioEvaluator(PortfolioProvider portfolioProvider) {
        this.portfolioProvider = portfolioProvider;
    }

    @Override
    public IgniteFuture<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            EvaluationContext evaluationContext) throws InterruptedException, ExecutionException {
        return evaluate(portfolio, null, evaluationContext);
    }

    @Override
    public IgniteFuture<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            Transaction transaction, EvaluationContext evaluationContext)
            throws ExecutionException, InterruptedException {
        return new IgniteFinishedFutureImpl<>(
                evaluate(portfolio.getRules(), portfolio, transaction, evaluationContext));
    }

    /**
     * Evaluates the given {@code Rule}s against the given {@code Portfolio}.
     *
     * @param rules
     *            the {@code Rule}s to be evaluated
     * @param portfolio
     *            the {code Portfolio} against which to evaluate the {@code Rule}s
     * @param transaction
     *            a (possibly {@code null} {@code Transaction} to optionally evaluate with the
     *            {@code Portfolio}
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return a {@code Map} associating each evaluated {@code Rule} with its result
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     * @throws ExcecutionException
     *             if the {@code Rule}s could not be processed
     */
    protected Map<RuleKey, EvaluationResult> evaluate(Stream<Rule> rules, Portfolio portfolio,
            Transaction transaction, EvaluationContext evaluationContext)
            throws ExecutionException, InterruptedException {
        LOG.info("locally evaluating Portfolio {} (\"{}\")", portfolio.getKey(),
                portfolio.getName());
        long startTime = System.currentTimeMillis();

        // evaluation may be short-circuited, but at least one short-circuiting result needs to be
        // included in the evaluation results for impact to be determined properly
        Map<RuleKey, EvaluationResult> shortCircuitResults =
                Collections.synchronizedMap(new HashMap<>());
        Predicate<EvaluationResult> shortCircuitPredicate;
        if (evaluationContext.getEvaluationMode() == EvaluationMode.SHORT_CIRCUIT_EVALUATION) {
            if (transaction == null) {
                // stop when a Rule fails
                shortCircuitPredicate = (result -> {
                    boolean isPassed = result.isPassed();
                    if (!isPassed) {
                        shortCircuitResults.put(result.getRuleKey(), result);
                    }

                    return isPassed;
                });
            } else {
                // stop when a non-compliant impact is encountered
                shortCircuitPredicate = (result -> {
                    TradeEvaluationResult tradeResult = new TradeEvaluationResult();
                    tradeResult.addImpacts(portfolio.getKey(), Map.of(result.getRuleKey(), result));
                    boolean isPassed = tradeResult.isCompliant();
                    if (!isPassed) {
                        shortCircuitResults.put(result.getRuleKey(), result);
                    }

                    return isPassed;
                });
            }
        } else {
            // take all results
            shortCircuitPredicate = (result -> true);
        }

        Map<RuleKey, EvaluationResult> results = ruleEvaluationThreadPool.submit(() -> rules
                .parallel()
                .map(r -> new RuleEvaluator(r, portfolio, transaction,
                        portfolio.getBenchmark(portfolioProvider), evaluationContext).call())
                .takeWhile(shortCircuitPredicate)
                .collect(Collectors.toMap(result -> result.getRuleKey(), result -> result))).get();

        Map<RuleKey, EvaluationResult> allResults =
                new HashMap<>(shortCircuitResults.size() + results.size());
        allResults.putAll(shortCircuitResults);
        allResults.putAll(results);
        LOG.info("evaluated {} Rules {} over {} Positions for Portfolio {} in {} ms",
                allResults.size(), (shortCircuitResults.isEmpty() ? "" : "(short-circuited)"),
                portfolio.size(), portfolio.getKey(), System.currentTimeMillis() - startTime);

        return allResults;
    }
}
