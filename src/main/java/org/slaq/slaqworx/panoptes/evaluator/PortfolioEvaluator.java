package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * A {@code PortfolioEvaluator} is responsible for the process of evaluating a set of {@code Rule}s
 * against some {@code Portfolio} and possibly some related benchmark. Processing may be local or
 * distributed based on the implementation.
 *
 * @author jeremy
 */
public interface PortfolioEvaluator {
    /**
     * Evaluates the given {@code Portfolio} using its associated Rules and benchmark (if any).
     * Right now this is the only evaluation mode capable of distributed processing and thus the
     * only method that appears in the interface.
     *
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
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> evaluate(Portfolio portfolio,
            EvaluationContext evaluationContext) throws InterruptedException, ExecutionException;
}
