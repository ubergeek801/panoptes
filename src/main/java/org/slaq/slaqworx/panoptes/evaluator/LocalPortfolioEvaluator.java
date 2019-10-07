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
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
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

    /**
     * Creates a new {@code LocalPortfolioEvaluator} that uses the common {@code ForkJoinPool} for
     * local {@code Portfolio} evaluation.
     */
    public LocalPortfolioEvaluator() {
        // nothing to do
    }

    @Override
    public IgniteFuture<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            EvaluationContext evaluationContext) throws InterruptedException, ExecutionException {
        // TODO maybe make evaluate() return an IgniteFuture as well
        return new IgniteFinishedFutureImpl<>(evaluate(portfolio.getRules(), portfolio, null,
                portfolio.getBenchmark(evaluationContext.getPortfolioProvider()),
                evaluationContext));
    }

    @Override
    public IgniteFuture<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            Transaction transaction, EvaluationContext evaluationContext)
            throws InterruptedException, ExecutionException {
        return new IgniteFinishedFutureImpl<>(evaluate(portfolio.getRules(), portfolio, transaction,
                portfolio.getBenchmark(evaluationContext.getPortfolioProvider()),
                evaluationContext));
    }

    @Override
    public IgniteFuture<Map<RuleKey, EvaluationResult>> evaluate(Stream<Rule> rules,
            Portfolio portfolio, EvaluationContext evaluationContext)
            throws ExecutionException, InterruptedException {
        return new IgniteFinishedFutureImpl<>(evaluate(rules, portfolio, null,
                portfolio.getBenchmark(evaluationContext.getPortfolioProvider()),
                evaluationContext));
    }

    /**
     * Evaluates, in parallel, the given {@code Position}s against the given {@code Rule}s and
     * (optionally) benchmark {@code Position}s. Each {@code Rule}'s results are grouped by its
     * specified {@code EvaluationGroup}.
     *
     * @param rules
     *            the {@code Rule}s to evaluate against the given {@code Portfolio}
     * @param portfolioPositions
     *            the {@code Portfolio} {@code Position}s to be evaluated
     * @param transaction
     *            the (possibly {@code null}) {@code Transaction} from which to include
     *            {@code Position}s
     * @param benchmarkPositions
     *            the (possibly {@code null}) benchmark {@code Position}s to be evaluated against
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return a {@code Map} associating each evaluated {@code Rule} with its result
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     * @throws ExcecutionException
     *             if the {@code Rule}s could not be processed
     */
    public Map<RuleKey, EvaluationResult> evaluate(Stream<Rule> rules,
            PositionSupplier portfolioPositions, Transaction transaction,
            PositionSupplier benchmarkPositions, EvaluationContext evaluationContext)
            throws ExecutionException, InterruptedException {
        LOG.info("locally evaluating Portfolio");
        long startTime = System.currentTimeMillis();
        final PositionSupplier portfolioPlusTransactionPositions =
                (transaction == null ? portfolioPositions
                        : new PositionSet(
                                Stream.concat(portfolioPositions.getPositions(),
                                        transaction.getPositions()),
                                portfolioPositions.getPortfolio()));

        Map<RuleKey, EvaluationResult> shortCircuitResults =
                Collections.synchronizedMap(new HashMap<>());
        Predicate<EvaluationResult> shortCircuitPredicate;
        switch (evaluationContext.getEvaluationMode()) {
        case PASS_SHORT_CIRCUIT_EVALUATION:
            // stop as soon as a Rule passes
            shortCircuitPredicate = (result -> {
                boolean isPassed = result.isPassed();
                if (isPassed) {
                    shortCircuitResults.put(result.getRuleKey(), result);
                }

                return !isPassed;
            });
            break;
        case FAIL_SHORT_CIRCUIT_EVALUATION:
            // stop as soon as a Rule fails
            shortCircuitPredicate = (result -> {
                boolean isPassed = result.isPassed();
                if (!isPassed) {
                    shortCircuitResults.put(result.getRuleKey(), result);
                }

                return isPassed;
            });
            break;
        default:
            // take all results
            shortCircuitPredicate = (result -> true);
        }

        Map<RuleKey, EvaluationResult> results = ruleEvaluationThreadPool
                .submit(() -> rules.parallel()
                        .map(r -> new RuleEvaluator(r, portfolioPlusTransactionPositions,
                                benchmarkPositions, evaluationContext).call())
                        .takeWhile(shortCircuitPredicate)
                        .collect(Collectors.toMap(result -> result.getRuleKey(), result -> result)))
                .get();

        Map<RuleKey, EvaluationResult> allResults =
                new HashMap<>(shortCircuitResults.size() + results.size());
        allResults.putAll(shortCircuitResults);
        allResults.putAll(results);
        LOG.info("evaluated {} Rules over {} Positions for Portfolio in {} ms", allResults.size(),
                portfolioPositions.size(), System.currentTimeMillis() - startTime);

        return allResults;
    }
}
