package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.data.PortfolioCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code PortfolioEvaluator} is responsible for the process of evaluating a set of {@code Rule}s
 * against some {@code Portfolio} and possibly some related benchmark.
 *
 * @author jeremy
 */
public class PortfolioEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(PortfolioEvaluator.class);

    private final PortfolioCache portfolioCache;

    /**
     * Creates a new {@code PortfolioEvaluator} that uses the common {@code ForkJoinPool} for local
     * {@code Rule} evaluation.
     */
    public PortfolioEvaluator() {
        this(null);
    }

    /**
     * Creates a new {@code PortfolioEvaluator} using the PortfolioCache cluster for distributed
     * {@code Rule} evaluation.
     *
     * @param portfolioCache
     *            the {@code PortfolioCache} to use to obtain distributed resources
     */
    public PortfolioEvaluator(PortfolioCache portfolioCache) {
        this.portfolioCache = portfolioCache;
    }

    /**
     * Evaluates the given {@code Portfolio} using its associated Rules and benchmark (if any).
     * Right now this is the only evaluation mode capable of distributed processing.
     *
     * @param portfolio
     *            the {@code Portfolio} to be evaluated
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return a {@code Map} associating each evaluated {@code Rule} with its result
     * @throws {@code
     *             InterruptedException} if the {@code Thread} is interrupted while awaiting results
     */
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> evaluate(Portfolio portfolio,
            EvaluationContext evaluationContext) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        try {
            if (portfolioCache == null) {
                // not distributed; use the local method
                LOG.info("locally evaluating Portfolio {} with {} Rules", portfolio.getName(),
                        portfolio.getRules().count());
                return evaluate(portfolio.getRules(), portfolio,
                        portfolio.getBenchmark(evaluationContext.getPortfolioProvider()),
                        evaluationContext);
            }

            // use the distributed method

            LOG.info("collectively evaluating Portfolio {} with {} Rules", portfolio.getName(),
                    portfolio.getRules().count());

            // set up a listener to get results
            UUID evaluationBatchId = UUID.randomUUID();
            RuleEvaluationResultListener resultListener = new RuleEvaluationResultListener();
            String listenerRegistration = portfolioCache.getRuleEvaluationResultMap()
                    .addEntryListener(resultListener, evaluationBatchId, true);
            try {
                // publish a message for each Portfolio/Rule to the evaluation queue
                // TODO consider using a Ringbuffer instead of IQueue, for batch insert capability
                int numExpectedResults = portfolio.getRules().map(r -> {
                    RuleEvaluationMessage ruleEvaluationMessage = new RuleEvaluationMessage(
                            evaluationBatchId, portfolio.getKey(), r.getKey());
                    portfolioCache.getRuleEvaluationQueue().add(ruleEvaluationMessage);

                    return ruleEvaluationMessage;
                }).collect(Collectors.summingInt(r -> 1));

                resultListener.join(numExpectedResults);
            } finally {
                portfolioCache.getRuleEvaluationResultMap()
                        .removeEntryListener(listenerRegistration);
                portfolioCache.getRuleEvaluationResultMap().delete(evaluationBatchId);
            }

            return resultListener.getResults();
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
     *            the (possibly null) benchmark to use in place of the {@code Portfolio}'s
     *            associated benchmark
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return a {@code Map} associating each evaluated {@code Rule} with its result
     */
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> evaluate(Portfolio portfolio,
            Portfolio benchmark, EvaluationContext evaluationContext) {
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
     */
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> evaluate(Portfolio portfolio,
            PositionSupplier positions, EvaluationContext evaluationContext) {
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
     */
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> evaluate(Stream<Rule> rules,
            Portfolio portfolio, EvaluationContext evaluationContext) {
        return evaluate(rules, portfolio,
                portfolio.getBenchmark(evaluationContext.getPortfolioProvider()),
                evaluationContext);
    }

    /**
     * Evaluates the given {@code Position}s against the given {@code Rule}s and (optionally)
     * benchmark {@code Position}s. Each {@code Rule}'s results are grouped by its specified
     * {@code EvaluationGroup}.
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
     */
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> evaluate(Stream<Rule> rules,
            PositionSupplier portfolioPositions, PositionSupplier benchmarkPositions,
            EvaluationContext evaluationContext) {
        return rules.parallel().collect(Collectors.toMap(r -> r.getKey(),
                r -> new RuleEvaluator(r, portfolioPositions, benchmarkPositions, evaluationContext)
                        .call()));
    }
}
