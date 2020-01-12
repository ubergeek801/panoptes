package org.slaq.slaqworx.panoptes.ui.compliance;

import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.rule.RuleResult.Threshold;

public interface EvaluationResultRow {
    public Double getBenchmarkValue();

    public int getChildCount();

    public Stream<EvaluationResultRow> getChildren();

    public String getGroup();

    public String getRuleDescription();

    public Threshold getThreshold();

    public Double getValue();

    public boolean hasChildren();

    public Boolean isPassed();
}
