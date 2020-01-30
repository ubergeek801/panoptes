package org.slaq.slaqworx.panoptes.ui.compliance;

import java.util.concurrent.ExecutionException;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import org.slaq.slaqworx.panoptes.ApplicationContextProvider;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.ClusterPortfolioEvaluator;
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

    private final PortfolioEvaluator portfolioEvaluator;
    private final AssetCache assetCache;

    private Portfolio portfolio;

    /**
     * Creates a new {@code CompliancePanel}.
     */
    public CompliancePanel() {
        portfolioEvaluator = ApplicationContextProvider.getApplicationContext()
                .getBean(ClusterPortfolioEvaluator.class);
        assetCache = ApplicationContextProvider.getApplicationContext().getBean(AssetCache.class);

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
            portfolio = assetCache.getPortfolio(new PortfolioKey(portfolioIdField.getValue(), 1));
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
        EvaluationResultPanel resultPanel = new EvaluationResultPanel();
        resultPanel.setSizeFull();
        add(resultPanel);

        run.addClickListener(event -> {
            if (portfolio == null) {
                return;
            }

            try {
                resultPanel.setResult(
                        portfolioEvaluator.evaluate(portfolio, new EvaluationContext()).get());
            } catch (InterruptedException | ExecutionException e) {
                // FIXME deal with this
                return;
            }
        });
    }
}
