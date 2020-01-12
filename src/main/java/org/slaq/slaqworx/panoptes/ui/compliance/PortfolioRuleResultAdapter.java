package org.slaq.slaqworx.panoptes.ui.compliance;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleResult;
import org.slaq.slaqworx.panoptes.rule.RuleResult.Threshold;

public class PortfolioRuleResultAdapter implements EvaluationResultRow {
    private static final Comparator<? super Entry<EvaluationGroup, RuleResult>> groupComparator =
            ((e1, o2) -> e1.getKey().getId().compareTo(o2.getKey().getId()));

    private final Map.Entry<RuleKey, EvaluationResult> evaluationResult;
    private final AssetCache assetCache;

    public PortfolioRuleResultAdapter(Map.Entry<RuleKey, EvaluationResult> evaluationResult,
            AssetCache assetCache) {
        this.evaluationResult = evaluationResult;
        this.assetCache = assetCache;
    }

    @Override
    public Double getBenchmarkValue() {
        // not applicable at this level
        return null;
    }

    @Override
    public int getChildCount() {
        return getGroupResults().size();
    }

    @Override
    public Stream<EvaluationResultRow> getChildren() {
        return getGroupResults().entrySet().stream().sorted(groupComparator)
                .map(e -> new GroupResultAdapter(this, e));
    }

    @Override
    public String getGroup() {
        // not applicable at this level
        return null;
    }

    public Rule getRule() {
        return assetCache.getRule(evaluationResult.getKey());
    }

    @Override
    public String getRuleDescription() {
        return getRule().getDescription() + " (" + getRule().getParameterDescription() + ")";
    }

    public String getShortRuleDescription() {
        return getRule().getDescription();
    }

    @Override
    public Threshold getThreshold() {
        // not applicable at this level
        return null;
    }

    @Override
    public Double getValue() {
        // not applicable at this level
        return null;
    }

    @Override
    public boolean hasChildren() {
        return !getGroupResults().isEmpty();
    }

    @Override
    public Boolean isPassed() {
        // not applicable at this level
        return null;
    }

    protected Map<EvaluationGroup, RuleResult> getGroupResults() {
        return evaluationResult.getValue().getResults();
    }
}
