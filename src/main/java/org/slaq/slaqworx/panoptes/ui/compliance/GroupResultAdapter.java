package org.slaq.slaqworx.panoptes.ui.compliance;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleResult;
import org.slaq.slaqworx.panoptes.rule.RuleResult.Threshold;

public class GroupResultAdapter implements EvaluationResultRow {
    private final PortfolioRuleResultAdapter parent;
    private final Map.Entry<EvaluationGroup, RuleResult> groupResult;

    public GroupResultAdapter(PortfolioRuleResultAdapter parent,
            Entry<EvaluationGroup, RuleResult> e) {
        this.parent = parent;
        groupResult = e;
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
    public boolean hasChildren() {
        return false;
    }

    @Override
    public Boolean isPassed() {
        return groupResult.getValue().isPassed();
    }
}
