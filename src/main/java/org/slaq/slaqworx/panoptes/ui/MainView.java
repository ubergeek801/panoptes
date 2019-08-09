package org.slaq.slaqworx.panoptes.ui;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import org.slaq.slaqworx.panoptes.data.PortfolioCache;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

@Route
@Push
public class MainView extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(MainView.class);

    private TextField statusTextField;
    private Button runComplianceButton;

    protected MainView(PortfolioCache portfolioCache) {
        statusTextField = new TextField();
        statusTextField.setReadOnly(true);
        statusTextField.setWidthFull();
        add(statusTextField);

        runComplianceButton = new Button("Run Compliance", VaadinIcon.EYE.create());
        runComplianceButton.addClickListener(e -> {
            long startTime = System.currentTimeMillis();
            PortfolioEvaluator evaluator = new PortfolioEvaluator(portfolioCache);
            int numPortfolios =
                    portfolioCache.getPortfolioCache().values().parallelStream().map(p -> {
                        setStatus("evaluating Portfolio " + p.getName());
                        try {
                            evaluator.evaluate(p, new EvaluationContext(portfolioCache,
                                    portfolioCache, portfolioCache));
                        } catch (InterruptedException ex) {
                            setStatus("evaluation thread was interrupted");
                        }
                        return null;
                    }).collect(Collectors.summingInt(p -> 1));
            long endTime = System.currentTimeMillis();
            setStatus("evaluated " + numPortfolios + " Portfolios in " + (endTime - startTime)
                    + " ms");
        });
        add(runComplianceButton);
    }

    protected void setStatus(String message) {
        LOG.info(message);
        getUI().get().access(() -> {
            statusTextField.setValue(message);
        });
    }
}
