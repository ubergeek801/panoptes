package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code LocalPortfolioEvaluator} is a {@code PortfolioEvaluator} which performs processing on the
 * local node. This is where parallelization of {@code Rule} processing occurs.
 *
 * @author jeremy
 */
public class LocalPortfolioEvaluator implements PortfolioEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(LocalPortfolioEvaluator.class);

    // TODO thread pool tuning is a work in progress
    private static final ForkJoinPool ruleEvaluationThreadPool =
            new ForkJoinPool(ForkJoinPool.getCommonPoolParallelism() + 2);

    /**
     * Creates a new {@code LocalPortfolioEvaluator} that uses the common {@code ForkJoinPool} for
     * local {@code Portfolio} evaluation.
     */
    public LocalPortfolioEvaluator() {
        // nothing to do
    }

    @Override
    public Future<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>>
            evaluate(Portfolio portfolio, EvaluationContext evaluationContext)
                    throws InterruptedException, ExecutionException {
        long numRules = portfolio.getRules().count();
        if (numRules == 0) {
            LOG.warn("not evaluating Portfolio {} with no Rules", portfolio.getName());
            return CompletableFuture.completedFuture(Collections.emptyMap());
        }

        long startTime = System.currentTimeMillis();
        try {
            LOG.info("locally evaluating Portfolio {} with {} Rules over {} Positions",
                    portfolio.getName(), numRules, portfolio.getPositions().count());
            // TODO maybe make evaluate() return a Future as well
            return CompletableFuture.completedFuture(evaluate(portfolio.getRules(), portfolio,
                    portfolio.getBenchmark(evaluationContext.getPortfolioProvider()),
                    evaluationContext));
        } finally {
            LOG.info("evaluated Portfolio {} in {} ms", portfolio.getName(),
                    System.currentTimeMillis() - startTime);
        }
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
        return evaluate(portfolio.getRules(), portfolio, benchmark, evaluationContext);
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
        return evaluate(portfolio.getRules(), positions,
                portfolio.getBenchmark(evaluationContext.getPortfolioProvider()),
                evaluationContext);
    }

    /**
     * Evaluates the given {@code Portfolio} against the given {@code Rule}s (instead of the
     * {@code Portfolio}'s own {@code Rule}s), using the {@code Portfolio}'s associated benchmark
     * (if any).
     *
     * @param rules
     *            the {@code Rule}s to evaluate against the given {@code Portfolio}
     * @param portfolio
     *            the {@code Portfolio} to be evaluated
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return a {@code Map} associating each evaluated {@code Rule} with its result
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     * @throws ExcecutionException
     *             if the {@code Rule}s could not be processed
     */
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> evaluate(Stream<Rule> rules,
            Portfolio portfolio, EvaluationContext evaluationContext)
            throws ExecutionException, InterruptedException {
        return evaluate(rules, portfolio,
                portfolio.getBenchmark(evaluationContext.getPortfolioProvider()),
                evaluationContext);
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
            PositionSupplier portfolioPositions, PositionSupplier benchmarkPositions,
            EvaluationContext evaluationContext) throws ExecutionException, InterruptedException {
        return ruleEvaluationThreadPool
                .submit(() -> rules.parallel()
                        .collect(
                                Collectors.toMap(r -> r.getKey(),
                                        r -> new RuleEvaluator(r, portfolioPositions,
                                                benchmarkPositions, evaluationContext).call())))
                .get();
    }
}
