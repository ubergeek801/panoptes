package org.slaq.slaqworx.panoptes.ui.compliance;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * {@code CompliancePanel} is a container providing tools for evaluating portfolio compliance. This
 * is very much a work in progress.
 *
 * @author jeremy
 */
public class CompliancePanel extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@code CompliancePanel}.
     */
    public CompliancePanel() {
        EvaluationResultPanel resultPanel = new EvaluationResultPanel();
        resultPanel.setSizeFull();
        add(resultPanel);
    }
}
