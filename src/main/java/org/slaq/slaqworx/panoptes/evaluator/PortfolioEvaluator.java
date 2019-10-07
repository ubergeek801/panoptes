package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.ignite.lang.IgniteFuture;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * A {@code PortfolioEvaluator} is responsible for the process of evaluating a set of {@code Rule}s
 * against some {@code Portfolio} and possibly some related benchmark. Processing may be local or
 * distributed based on the implementation.
 *
 * @author jeremy
 */
public interface PortfolioEvaluator {
    /**
     * Evaluates the given {@code Portfolio} using its associated {@code Rule}s and benchmark (if
     * any).
     *
     * @param portfolio
     *            the {@code Portfolio} to be evaluated
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return an {@code IgniteFuture} {@code Map} associating each evaluated {@code Rule} with its
     *         result
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     * @throws ExcecutionException
     *             if the {@code Rule}s could not be processed
     */
    public IgniteFuture<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            EvaluationContext evaluationContext) throws InterruptedException, ExecutionException;

    /**
     * Evaluates the combined {@code Position}s of the given {@code Portfolio} and
     * {@code Transaction} using the {@code Portfolio} {@code Rule}s and benchmark (if any).
     *
     * @param portfolio
     *            the {@code Portfolio} to be evaluated
     * @param transaction
     *            the {@code Transaction} from which to include allocation {@code Position}s for
     *            evaluation
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return an {@code IgniteFuture} {@code Map} associating each evaluated {@code Rule} with its
     *         result
     * @throws InterruptedException
     *             if the {@code Thread} was interrupted during processing
     * @throws ExcecutionException
     *             if the {@code Rule}s could not be processed
     */
    public IgniteFuture<Map<RuleKey, EvaluationResult>> evaluate(Portfolio portfolio,
            Transaction transaction, EvaluationContext evaluationContext)
            throws InterruptedException, ExecutionException;
}
