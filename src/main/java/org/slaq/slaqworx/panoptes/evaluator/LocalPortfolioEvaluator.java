package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.internal.util.future.IgniteFinishedFutureImpl;
import org.apache.ignite.lang.IgniteFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
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
    public IgniteFuture<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>>
            evaluate(Portfolio portfolio, EvaluationContext evaluationContext)
                    throws InterruptedException, ExecutionException {
        // TODO maybe make evaluate() return an IgniteFuture as well
        return new IgniteFinishedFutureImpl<>(evaluate(portfolio.getRules(), portfolio, null,
                portfolio.getBenchmark(evaluationContext.getPortfolioProvider()),
                evaluationContext));
    }

    /**
     * Evaluates the given {@code Portfolio} using its associated {@code Rule}s but overriding its
     * associated benchmark (if any) with the specified benchmark.
     *
     * @param portfolio
     *            the {@code Portfolio} to be evaluated
     * @param benchmark
     *            the (possibly {@code null}) benchmark to use in place of the {@code Portfolio}'s
     *            associated benchmark
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return a {@code Map} associating each evaluated {@code Rule} with its result
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     * @throws ExcecutionException
     *             if the {@code Rule}s could not be processed
     */
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> evaluate(Portfolio portfolio,
            Portfolio benchmark, EvaluationContext evaluationContext)
            throws ExecutionException, InterruptedException {
        return evaluate(portfolio.getRules(), portfolio, null, benchmark, evaluationContext);
    }

    /**
     * Evaluates the given {@code Portfolio} using its associated {@code Rule}s and benchmark (if
     * any), but overriding its {@code Position}s with the specified {@code Position}s.
     *
     * @param portfolio
     *            the {@code Portfolio} to be evaluated
     * @param positions
     *            the {@code Positions} to use in place of the {@code Portfolio}'s own
     *            {@code Position}s
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return a {@code Map} associating each evaluated {@code Rule} with its result
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     * @throws ExcecutionException
     *             if the {@code Rule}s could not be processed
     */
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> evaluate(Portfolio portfolio,
            PositionSupplier positions, EvaluationContext evaluationContext)
            throws ExecutionException, InterruptedException {
        return evaluate(portfolio.getRules(), positions, null,
                portfolio.getBenchmark(evaluationContext.getPortfolioProvider()),
                evaluationContext);
    }

    @Override
    public IgniteFuture<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> evaluate(
            Portfolio portfolio, Transaction transaction, EvaluationContext evaluationContext)
            throws InterruptedException, ExecutionException {
        return new IgniteFinishedFutureImpl<>(evaluate(portfolio.getRules(), portfolio, transaction,
                portfolio.getBenchmark(evaluationContext.getPortfolioProvider()),
                evaluationContext));
    }

    @Override
    public IgniteFuture<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>>
            evaluate(Stream<Rule> rules, Portfolio portfolio, EvaluationContext evaluationContext)
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
     * @return a {@code Map} associating each evaluated {@code Rule} with its (grouped) result
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     * @throws ExcecutionException
     *             if the {@code Rule}s could not be processed
     */
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> evaluate(Stream<Rule> rules,
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

        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> shortCircuitResults =
                Collections.synchronizedMap(new HashMap<>());
        Predicate<Pair<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> shortCircuitPredicate;
        switch (evaluationContext.getEvaluationMode()) {
        case PASS_SHORT_CIRCUIT_EVALUATION:
            // stop as soon as a Rule passes
            shortCircuitPredicate = (result -> {
                boolean isPassed = EvaluationResult.isPassed(result.getRight());
                if (isPassed) {
                    shortCircuitResults.put(result.getLeft(), result.getRight());
                }

                return !isPassed;
            });
            break;
        case FAIL_SHORT_CIRCUIT_EVALUATION:
            // stop as soon as a Rule fails
            shortCircuitPredicate = (result -> {
                boolean isPassed = EvaluationResult.isPassed(result.getRight());
                if (!isPassed) {
                    shortCircuitResults.put(result.getLeft(), result.getRight());
                }

                return isPassed;
            });
            break;
        default:
            // take all results
            shortCircuitPredicate = (result -> true);
        }

        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results = ruleEvaluationThreadPool
                .submit(() -> rules.parallel()
                        .map(r -> new ImmutablePair<>(r.getKey(),
                                new RuleEvaluator(r, portfolioPlusTransactionPositions,
                                        benchmarkPositions, evaluationContext).call()))
                        .takeWhile(shortCircuitPredicate)
                        .collect(Collectors.toMap(pair -> pair.getLeft(), pair -> pair.getRight())))
                .get();

        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> allResults =
                new HashMap<>(shortCircuitResults.size() + results.size());
        allResults.putAll(shortCircuitResults);
        allResults.putAll(results);
        LOG.info("evaluated {} Rules over {} Positions for Portfolio in {} ms", allResults.size(),
                portfolioPositions.size(), System.currentTimeMillis() - startTime);

        return allResults;
    }
}
