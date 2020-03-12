package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * {@code LocalPortfolioEvaluator} is a {@code PortfolioEvaluator} which performs processing on the
 * local node. {@code Rule}s are evaluated sequentially (although in no particular order); any
 * desired processing concurrency is expected to be provided at a higher layer of abstraction.
 *
 * @author jeremy
 */
public class LocalPortfolioEvaluator implements PortfolioEvaluator {
    /**
     * {@code ShortCircuitingResultMapper} is a {@code Function} intended to {@code flatMap()} a
     * {@code Stream} of {@code EvaluationResult}s, short-circuiting the {@code Stream} (by mapping
     * subsequent elements to empty streams) after a failed result is encountered.
     * <p>
     * Note that a {@code Function} used by {@code flatMap()} is expected to be stateless; we bend
     * that definition somewhat by maintaining state (specifically, whether a failed result has
     * already been seen). However, this is consistent with the semantics of short-circuit
     * evaluation, which allow any number of results to be returned when an evaluation is
     * short-circuited, as long as at least one of them indicates a failure.
     *
     * @author jeremy
     */
    private static class ShortCircuitingResultMapper
            implements Function<EvaluationResult, Stream<EvaluationResult>> {
        private final PortfolioKey portfolioKey;
        private final Transaction transaction;
        private final boolean isShortCircuiting;

        private EvaluationResult failedResult;

        /**
         * Creates a new {@code ShortCircuitingResultMapper}.
         *
         * @param portfolioKey
         *            a {@code PortfolioKey} identifying the {@code Portfolio} being evaluated
         * @param transaction
         *            a (possibly {@code null} {@code Transaction} being evaluated with the
         *            {@code Portfolio}
         * @param isShortCircuiting
         *            {@code true} if short-circuiting is to be activated, {@code false} to allow
         *            all evaluations to pass through
         */
        public ShortCircuitingResultMapper(PortfolioKey portfolioKey, Transaction transaction,
                boolean isShortCircuiting) {
            this.portfolioKey = portfolioKey;
            this.transaction = transaction;
            this.isShortCircuiting = isShortCircuiting;
        }

        @Override
        public Stream<EvaluationResult> apply(EvaluationResult result) {
            if (!isShortCircuiting) {
                // always pass the result through
                return Stream.of(result);
            }

            if (failedResult != null) {
                // no need to visit any more results
                return Stream.empty();
            }

            boolean isPassed;
            if (transaction == null) {
                // stop when a Rule fails
                isPassed = result.isPassed();
            } else {
                // stop when a non-compliant impact is encountered
                TradeEvaluationResult tradeResult = new TradeEvaluationResult();
                tradeResult.addImpacts(portfolioKey, Map.of(result.getRuleKey(), result));
                isPassed = tradeResult.isCompliant();
            }

            if (!isPassed) {
                failedResult = result;
            }

            return Stream.of(result);
        }

        /**
         * Indicates whether a short-circuit was triggered.
         *
         * @return {@code true} if at least one evaluation failed and triggered a short-circuit,
         *         {@code false} otherwise
         */
        public boolean isShortCircuited() {
            return (failedResult != null);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(LocalPortfolioEvaluator.class);

    private final PortfolioProvider portfolioProvider;

    /**
     * Creates a new {@code LocalPortfolioEvaluator}.
     *
     * @param portfolioProvider
     *            the {@code PortfolioProvider} to use to resolve {@code Portfolio} references
     */
    public LocalPortfolioEvaluator(PortfolioProvider portfolioProvider) {
        this.portfolioProvider = portfolioProvider;
    }

    @Override
    public Future<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            EvaluationContext evaluationContext) {
        return evaluate(portfolio, null, evaluationContext);
    }

    @Override
    public Future<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            Transaction transaction, EvaluationContext evaluationContext) {
        return CompletableFuture.completedFuture(
                evaluate(portfolio.getRules(), portfolio, transaction, evaluationContext));
    }

    /**
     * Evaluates the given {@code Rule}s against the given {@code Portfolio}.
     *
     * @param rules
     *            the {@code Rule}s to be evaluated
     * @param portfolio
     *            the {@code Portfolio} against which to evaluate the {@code Rule}s
     * @param transaction
     *            a (possibly {@code null} {@code Transaction} to optionally evaluate with the
     *            {@code Portfolio}
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return a {@code Map} associating each evaluated {@code Rule} with its result
     */
    protected Map<RuleKey, EvaluationResult> evaluate(Stream<Rule> rules, Portfolio portfolio,
            Transaction transaction, EvaluationContext evaluationContext) {
        LOG.info("locally evaluating Portfolio {} (\"{}\")", portfolio.getKey(),
                portfolio.getName());
        long startTime = System.currentTimeMillis();

        ShortCircuitingResultMapper shortCircuiter = new ShortCircuitingResultMapper(
                portfolio.getKey(), transaction,
                evaluationContext.getEvaluationMode() == EvaluationMode.SHORT_CIRCUIT_EVALUATION);

        // evaluate the Rules
        Map<RuleKey, EvaluationResult> results = rules
                .map(r -> new RuleEvaluator(r, portfolio, transaction,
                        portfolio.getBenchmark(portfolioProvider), evaluationContext).call())
                .flatMap(shortCircuiter)
                .collect(Collectors.toMap(result -> result.getRuleKey(), result -> result));

        LOG.info("evaluated {} Rules ({}) over {} Positions for Portfolio {} in {} ms",
                results.size(), (shortCircuiter.isShortCircuited() ? "short-circuited" : "full"),
                portfolio.size(), portfolio.getKey(), System.currentTimeMillis() - startTime);

        return results;
    }
}
