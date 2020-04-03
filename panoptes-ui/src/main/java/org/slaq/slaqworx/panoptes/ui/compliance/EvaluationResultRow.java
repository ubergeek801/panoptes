package org.slaq.slaqworx.panoptes.ui.compliance;

import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.rule.RuleResult.Threshold;

/**
 * {@code EvaluationResultRow} is an interface that facilitates adaptation of
 * {@code EvaluationResult}s into a tabular (or hierarchical/tabular hybrid) projection. Note that
 * any data accessor method may return {@code null} if the value is not appropriate for the current
 * hierarchy level, in addition to the usual reasons.
 *
 * @author jeremy
 */
public interface EvaluationResultRow {
    /**
     * Obtains the value associated with this row's result, as calculated for the benchmark, if any.
     *
     * @return the value associated with this row's result (if value-based), or {@code null} if it
     *         does not exist (Boolean-based or value-based with no benchmark specified)
     */
    public Double getBenchmarkValue();

    /**
     * Obtains the number of children of this row.
     *
     * @return this row's number of children
     */
    public int getChildCount();

    /**
     * Obtains this row's children as a {@code Stream}.
     *
     * @return a {@code Stream} of this row's children
     */
    public Stream<EvaluationResultRow> getChildren();

    /**
     * Obtains the {@code EvaluationGroup} corresponding to this row's result, in {@code String}
     * form.
     *
     * @return a {@code String} representation of this row's {@code EvaluationGroup}
     */
    public String getGroup();

    /**
     * Obtains a (more or less) human-readable description of the {@code Rule} that produced this
     * row's result.
     *
     * @return the {@code Rule} description
     */
    public String getRuleDescription();

    /**
     * Obtains the {@code Threshold} associated with this row's result.
     *
     * @return the {@code Threshold} applied by the {@code Rule} that generated this row's result
     */
    public Threshold getThreshold();

    /**
     * Obtains the value associated with this row's result.
     *
     * @return the value associated with this row's result (if value-based), or {@code null} if it
     *         does not exist (Boolean-based)
     */
    public Double getValue();

    /**
     * Indicates whether the {@code Rule} evaluation associated with this row passed.
     *
     * @return {@code true} if the evaluation passed, {@code false} otherwise
     */
    public Boolean isPassed();
}
