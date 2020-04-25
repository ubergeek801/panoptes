package org.slaq.slaqworx.panoptes.ui.compliance;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.renderer.NumberRenderer;

import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code EvaluationResultPanel} presents the results of portfolio compliance evaluation. This is
 * very much a work in progress.
 *
 * @author jeremy
 */
public class EvaluationResultPanel extends TreeGrid<EvaluationResultRow> {
    private static final long serialVersionUID = 1L;

    private static final NumberFormat valueNumberFormat;

    static {
        valueNumberFormat = NumberFormat.getInstance();
        valueNumberFormat.setMaximumFractionDigits(4);
    }

    private final AssetCache assetCache;
    private final HierarchicalDataProvider<EvaluationResultRow, Void> dataProvider;

    private List<EvaluationResultRow> portfolioResults = Collections.emptyList();

    /**
     * Creates a new {@code EvaluationResultPanel}.
     *
     * @param assetCache
     *            the {@code AssetCache} to use to resolve cached entities
     */
    public EvaluationResultPanel(AssetCache assetCache) {
        this.assetCache = assetCache;

        addHierarchyColumn(EvaluationResultRow::getRuleDescription).setAutoWidth(true)
                .setHeader("Rule");
        addColumn(EvaluationResultRow::getGroup).setAutoWidth(true).setHeader("Group");
        addColumn(new NumberRenderer<>(EvaluationResultRow::getValue, valueNumberFormat))
                .setAutoWidth(true).setHeader("Value");
        addColumn(new NumberRenderer<>(EvaluationResultRow::getBenchmarkValue, valueNumberFormat))
                .setAutoWidth(true).setHeader("Benchmark");
        addColumn(EvaluationResultRow::getThreshold).setAutoWidth(true).setHeader("Threshold");
        addColumn(r -> r.isPassed() == null ? null : (r.isPassed() ? "Pass" : "Fail"))
                .setAutoWidth(true).setHeader("Result");

        dataProvider = new AbstractBackEndHierarchicalDataProvider<>() {
            private static final long serialVersionUID = 1L;

            @Override
            public int getChildCount(HierarchicalQuery<EvaluationResultRow, Void> query) {
                if (query.getParent() == null) {
                    return portfolioResults.size();
                }
                return query.getParent().getChildCount();
            }

            @Override
            public boolean hasChildren(EvaluationResultRow item) {
                if (item == null) {
                    return !portfolioResults.isEmpty();
                }
                return item.getChildCount() > 0;
            }

            @Override
            public boolean isInMemory() {
                return true;
            }

            @Override
            protected Stream<EvaluationResultRow>
                    fetchChildrenFromBackEnd(HierarchicalQuery<EvaluationResultRow, Void> query) {
                if (query.getParent() == null) {
                    return portfolioResults.stream();
                }
                return query.getParent().getChildren();
            }
        };
        setDataProvider(dataProvider);
    }

    /**
     * Sets the result to be displayed by this panel.
     *
     * @param evaluationResult
     *            a {@code Map} of {@code RuleKey}-related {@code EvaluationResult}s to be displayed
     */
    public void setResult(Map<RuleKey, EvaluationResult> evaluationResult) {
        Comparator<? super EvaluationResultRow> comparator =
                (r1, r2) -> r1.getRuleDescription().compareTo(r2.getRuleDescription());
        portfolioResults = evaluationResult.entrySet().stream()
                .map(e -> new PortfolioRuleResultAdapter(e, assetCache)).sorted(comparator)
                .collect(Collectors.toList());

        dataProvider.refreshAll();
    }
}
