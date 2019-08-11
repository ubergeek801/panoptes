package org.slaq.slaqworx.panoptes.ui;

import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import org.slaq.slaqworx.panoptes.data.PortfolioCache;
import org.slaq.slaqworx.panoptes.evaluator.ClusterPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

@Route
@Push
public class MainView extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    // use a dedicated thread pool to inject evaluation requests
    private static final ForkJoinPool portfolioEvaluationThreadPool = new ForkJoinPool(100);
    private TextField statusTextField;

    private Button runComplianceButton;

    protected MainView(PortfolioCache portfolioCache, ClusterPortfolioEvaluator portfolioEvaluator) {
        statusTextField = new TextField();
        statusTextField.setReadOnly(true);
        statusTextField.setWidthFull();
        add(statusTextField);

        runComplianceButton = new Button("Run Compliance", VaadinIcon.EYE.create());
        runComplianceButton.addClickListener(e -> {
            long startTime = System.currentTimeMillis();
            int numPortfolios;
            try {
                numPortfolios = portfolioEvaluationThreadPool.submit(() -> portfolioCache
                        .getPortfolioCache().values().parallelStream().map(p -> {
                            setStatus("evaluating Portfolio " + p.getName());
                            try {
                                portfolioEvaluator.evaluate(p, new EvaluationContext(portfolioCache,
                                        portfolioCache, portfolioCache));
                            } catch (InterruptedException ex) {
                                setStatus("evaluation thread was interrupted");
                            }
                            return 1;
                        }).collect(Collectors.summingInt(p -> p))).get();
            } catch (Exception ex) {
                setStatus("could not evaluate Portfolios");
                return;
            }
            long endTime = System.currentTimeMillis();
            setStatus("evaluated " + numPortfolios + " Portfolios in " + (endTime - startTime)
                    + " ms");
        });

        add(runComplianceButton);
    }

    protected void setStatus(String message) {
        getUI().get().access(() -> {
            statusTextField.setValue(message);
        });
    }
}
