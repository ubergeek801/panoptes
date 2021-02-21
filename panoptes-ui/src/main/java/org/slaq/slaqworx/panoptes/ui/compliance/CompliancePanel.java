package org.slaq.slaqworx.panoptes.ui.compliance;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.cache.PortfolioSummarizer;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.ui.ComponentUtil;

/**
 * {@code CompliancePanel} is a container providing tools for evaluating portfolio compliance. This
 * is very much a work in progress.
 *
 * @author jeremy
 */
public class CompliancePanel extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    private PortfolioSummary portfolio;

    /**
     * Creates a new {@code CompliancePanel}.
     *
     * @param portfolioEvaluator
     *            the {@code PortfolioEvaluator} to use to perform compliance evaluation
     * @param assetCache
     *            the {@code AssetCache} to use to resolve cached entities
     */
    public CompliancePanel(PortfolioEvaluator portfolioEvaluator, AssetCache assetCache) {
        HorizontalLayout portfolioSelectionPanel = new HorizontalLayout();
        TextField portfolioIdField = ComponentUtil.createTextField("Portfolio ID");
        portfolioIdField.setValueChangeMode(ValueChangeMode.EAGER);
        portfolioSelectionPanel.add(portfolioIdField);

        TextField portfolioNameField = ComponentUtil.createTextField(null);
        portfolioNameField.setReadOnly(true);
        portfolioSelectionPanel.addAndExpand(portfolioNameField);

        Button run = ComponentUtil.createButton("Run");
        portfolioSelectionPanel.add(run);

        // add event listeners

        portfolioIdField.addValueChangeListener(event -> {
            // FIXME use a proper version
            PortfolioKey portfolioKey = new PortfolioKey(portfolioIdField.getValue(), 1);
            portfolio = assetCache.getPortfolioCache().executeOnKey(portfolioKey,
                    new PortfolioSummarizer(new EvaluationContext()));
            if (portfolio == null) {
                portfolioIdField.setErrorMessage("not found");
                portfolioIdField.setInvalid(true);
                run.setEnabled(false);
                return;
            }

            portfolioIdField.setInvalid(false);
            portfolioNameField.setValue(portfolio.getName());
            run.setEnabled(true);
        });

        portfolioSelectionPanel.setWidthFull();
        add(portfolioSelectionPanel);
        EvaluationResultPanel resultPanel = new EvaluationResultPanel(assetCache);
        resultPanel.setSizeFull();
        add(resultPanel);

        run.addClickListener(event -> {
            if (portfolio == null) {
                return;
            }

            resultPanel.setResult(portfolioEvaluator
                    .evaluate(portfolio.getKey(), new EvaluationContext()).join());
        });
    }
}
