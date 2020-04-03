package org.slaq.slaqworx.panoptes.ui.compliance;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleResult;
import org.slaq.slaqworx.panoptes.rule.RuleResult.Threshold;

/**
 * {@code GroupResultAdapter} adapts evaluation group-level results to a tabular representation. Its
 * parent is typically a {@code PortfolioRuleResultAdapter}.
 *
 * @author jeremy
 */
public class GroupResultAdapter implements EvaluationResultRow {
    private final PortfolioRuleResultAdapter parent;
    private final Map.Entry<EvaluationGroup, RuleResult> groupResult;

    /**
     * Creates a new {@code GroupResultAdapter} with the given parent and mapping the given group
     * result.
     *
     * @param parent
     *            the parent row of this row
     * @param groupResult
     *            the {@code EvaluationGroup} and its corresponding {@code RuleResult} to be adapted
     *            to row format
     */
    public GroupResultAdapter(PortfolioRuleResultAdapter parent,
            Entry<EvaluationGroup, RuleResult> groupResult) {
        this.parent = parent;
        this.groupResult = groupResult;
    }

    @Override
    public Double getBenchmarkValue() {
        return groupResult.getValue().getBenchmarkValue();
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public Stream<EvaluationResultRow> getChildren() {
        return Stream.empty();
    }

    @Override
    public String getGroup() {
        return groupResult.getKey().getId();
    }

    @Override
    public String getRuleDescription() {
        return parent.getShortRuleDescription();
    }

    @Override
    public Threshold getThreshold() {
        return groupResult.getValue().getThreshold();
    }

    @Override
    public Double getValue() {
        return groupResult.getValue().getValue();
    }

    @Override
    public Boolean isPassed() {
        return groupResult.getValue().isPassed();
    }
}
