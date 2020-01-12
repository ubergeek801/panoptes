package org.slaq.slaqworx.panoptes.ui.compliance;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

import org.slaq.slaqworx.panoptes.ApplicationContextProvider;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.ClusterPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code EvaluationResultPanel} presents the results of portfolio compliance evaluation. This is
 * very much a work in progress.
 *
 * @author jeremy
 */
public class EvaluationResultPanel extends TreeGrid<EvaluationResultRow> {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@code EvaluationResultPanel}.
     */
    public EvaluationResultPanel() {
        addHierarchyColumn(EvaluationResultRow::getRuleDescription).setAutoWidth(true)
                .setHeader("Rule");
        addColumn(EvaluationResultRow::getGroup).setAutoWidth(true).setHeader("Group");
        addColumn(EvaluationResultRow::getValue).setAutoWidth(true).setHeader("Value");
        addColumn(EvaluationResultRow::getBenchmarkValue).setAutoWidth(true).setHeader("Benchmark");
        addColumn(EvaluationResultRow::getThreshold).setAutoWidth(true).setHeader("Threshold");
        addColumn(r -> r.isPassed() == null ? null : (r.isPassed() ? "Pass" : "Fail"))
                .setAutoWidth(true).setHeader("Result");

        AssetCache assetCache =
                ApplicationContextProvider.getApplicationContext().getBean(AssetCache.class);
        PortfolioEvaluator portfolioEvaluator = ApplicationContextProvider.getApplicationContext()
                .getBean(ClusterPortfolioEvaluator.class);

        PortfolioKey portfolioKey = new PortfolioKey("test170", 1);
        Portfolio portfolio = assetCache.getPortfolio(portfolioKey);
        Map<RuleKey, EvaluationResult> evaluationResult;
        try {
            evaluationResult =
                    portfolioEvaluator.evaluate(portfolio, new EvaluationContext()).get();
        } catch (InterruptedException | ExecutionException e) {
            // FIXME deal with this
            return;
        }
        Comparator<? super EvaluationResultRow> comparator =
                (r1, r2) -> r1.getRuleDescription().compareTo(r2.getRuleDescription());
        List<EvaluationResultRow> portfolioResults = evaluationResult.entrySet().stream()
                .map(e -> new PortfolioRuleResultAdapter(e, assetCache)).sorted(comparator)
                .collect(Collectors.toList());

        HierarchicalDataProvider<EvaluationResultRow, Void> dataProvider =
                new AbstractBackEndHierarchicalDataProvider<>() {
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
                    protected Stream<EvaluationResultRow> fetchChildrenFromBackEnd(
                            HierarchicalQuery<EvaluationResultRow, Void> query) {
                        if (query.getParent() == null) {
                            return portfolioResults.stream();
                        }
                        return query.getParent().getChildren();
                    }
                };

        setDataProvider(dataProvider);
    }
}
