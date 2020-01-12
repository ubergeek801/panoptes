package org.slaq.slaqworx.panoptes.ui.trading;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import org.slaq.slaqworx.panoptes.ui.ComponentUtil;

/**
 * {@code SecurityFilterPanel} is a component of the experimental user interface, providing the
 * means to filter the master security list by a variety of attributes.
 *
 * @author jeremy
 */
public class SecurityFilterPanel extends FormLayout {
    private static final long serialVersionUID = 1L;

    private static final int NUM_COLUMNS = 7; // TODO this isn't very "responsive"

    /**
     * Creates a new {@code SecurityFilterPanel}.
     */
    public SecurityFilterPanel() {
        setResponsiveSteps(new ResponsiveStep("1em", NUM_COLUMNS));

        add(ComponentUtil.createTextField("Asset ID"));
        add(ComponentUtil.createTextField("CUSIP"));
        add(ComponentUtil.createTextField("Description"), 2);
        add(ComponentUtil.createTextField("Country"));
        add(ComponentUtil.createTextField("Region"));
        add(ComponentUtil.createTextField("Sector"));
        add(ComponentUtil.createTextField("Currency"));
        add(ComponentUtil.createMinMaxNumberField("Coupon"), 2);
        add(ComponentUtil.createMinMaxDateField("Maturity"), 2);
        add(ComponentUtil.createMinMaxNumberField("Rating"), 2);
        add(ComponentUtil.createMinMaxNumberField("Yield"), 2);
        add(ComponentUtil.createMinMaxNumberField("Duration"), 2);
        add(ComponentUtil.createTextField("Issuer"));
        add(ComponentUtil.createMinMaxNumberField("Price"), 2);

        Button filter = ComponentUtil.createButton("Filter", event -> {
            // FIXME implement listener
        });
        Button reset = ComponentUtil.createButton("Reset", event -> {
            // FIXME implement listener
        });
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(filter, reset);
        filter.getStyle().set("margin-right", "0.3em");
        add(actions, NUM_COLUMNS);
    }
}
