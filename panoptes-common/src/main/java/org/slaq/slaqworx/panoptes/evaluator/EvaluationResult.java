package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * {@code EvaluationResult} aggregates {@code RuleResult}s for a single {@code Rule} evaluation.
 *
 * @author jeremy
 */
public class EvaluationResult implements Keyed<RuleKey>, ProtobufSerializable {
    private final RuleKey ruleKey;
    private final Map<EvaluationGroup, ValueResult> results;
    private final Map<EvaluationGroup, ValueResult> proposedResults;

    /**
     * Creates a new {@code EvaluationResult} for the specified {@code Rule} using the given grouped
     * {@code RuleResult}s.
     *
     * @param ruleKey
     *            the key indicating the {@code Rule} for which the results were produced
     * @param results
     *            a {@code Map} containing the grouped evaluation results
     */
    public EvaluationResult(RuleKey ruleKey, Map<EvaluationGroup, ValueResult> results) {
        this(ruleKey, results, null);
    }

    /**
     * Creates a new {@code EvaluationResult} for the specified {@code Rule} using the given grouped
     * {@code RuleResult}s and proposed {@code RuleResult}s.
     *
     * @param ruleKey
     *            the key indicating the {@code Rule} for which the results were produced
     * @param results
     *            a {@code Map} containing the grouped evaluation results
     * @param proposedResults
     *            a (possibly {@code null} {@code Map} containing the grouped evaluation results of
     *            a proposed set of {@code Position}s, if requested
     */
    public EvaluationResult(RuleKey ruleKey, Map<EvaluationGroup, ValueResult> results,
            Map<EvaluationGroup, ValueResult> proposedResults) {
        this.ruleKey = ruleKey;
        this.results = results;
        this.proposedResults = (proposedResults == null ? Collections.emptyMap() : proposedResults);
    }

    @Override
    public RuleKey getKey() {
        return ruleKey;
    }

    /**
     * Obtains the proposed result corresponding to the specified group, if any.
     *
     * @param group
     *            the group for which to obtain proposed results
     * @return a {@code RuleResult} describing the requested proposed results, or {@code null} if
     *         there were no proposed results for the specified group
     */
    public ValueResult getProposedResult(EvaluationGroup group) {
        return proposedResults.get(group);
    }

    /**
     * Obtains the proposed results aggregated by this {@code EvaluationResult}.
     *
     * @return a {@code Map} of {@code EvaluationGroup} to its corresponding proposed result
     */
    public Map<EvaluationGroup, ValueResult> getProposedResults() {
        return proposedResults;
    }

    /**
     * Obtains the result corresponding to the specified group, if any.
     *
     * @param group
     *            the group for which to obtain results
     * @return a {@code RuleResult} describing the requested results, or {@code null} if there were
     *         no results for the specified group
     */
    public ValueResult getResult(EvaluationGroup group) {
        return results.get(group);
    }

    /**
     * Obtains the results aggregated by this {@code EvaluationResult}.
     *
     * @return a {@code Map} of {@code EvaluationGroup} to its corresponding result
     */
    public Map<EvaluationGroup, ValueResult> getResults() {
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
        return results.values().stream().allMatch(ValueResult::isPassed);
    }

    /**
     * Obtains the number of aggregated groups/results.
     *
     * @return the number of results in this {@code EvaluationResult}
     */
    public int size() {
        return results.size();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
