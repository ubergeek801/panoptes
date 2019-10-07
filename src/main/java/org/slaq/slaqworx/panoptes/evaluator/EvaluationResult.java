package org.slaq.slaqworx.panoptes.evaluator;

import java.io.Serializable;
import java.util.Map;

import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleResult;

/**
 * {@code EvaluationResult} aggregates {@code RuleResult}s for a single {@code Rule} evaluation.
 *
 * @author jeremy
 */
public class EvaluationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final RuleKey ruleKey;
    private final Map<EvaluationGroup<?>, RuleResult> results;

    /**
     * Creates a new {@code EvaluationResult} for the specified {@code Rule} using the given grouped
     * {@code RuleResult}s.
     *
     * @param ruleKey
     *            the key indicating the {@code Rule} for which the results were produced
     * @param results
     *            the evaluation results
     */
    public EvaluationResult(RuleKey ruleKey, Map<EvaluationGroup<?>, RuleResult> results) {
        this.ruleKey = ruleKey;
        this.results = results;
    }

    /**
     * Obtains the result corresponding to the specified group, if any.
     *
     * @param group
     *            the group for which to obtain results
     * @return a {@code RuleResult} describing the requested results, or {@code null} if there were
     *         no results for the specified group
     */
    public RuleResult getResult(EvaluationGroup<?> group) {
        return results.get(group);
    }

    /**
     * Obtains the results aggregated by this {@code EvaluationResult}.
     *
     * @return a {@code Map} of {@code EvaluationGroup} to its corresponding result
     */
    public Map<EvaluationGroup<?>, RuleResult> getResults() {
        return results;
    }

    /**
     * Obtains the key identifying the {@code Rule} for which these results were produced.
     *
     * @return a {@code RuleKey}
     */
    public RuleKey getRuleKey() {
        return ruleKey;
    }

    /**
     * Determines whether the aggregated results indicate a pass or failure.
     *
     * @return {@code true} if each of the individual results indicates a pass, {@code false} if at
     *         least one indicates failure
     */
    public boolean isPassed() {
        return results.values().stream().allMatch(r -> r.isPassed());
    }

    /**
     * Obtains the number of aggregated groups/results.
     *
     * @return the number of results in this {@code EvaluationResult}
     */
    public Integer size() {
        return results.size();
    }
}
