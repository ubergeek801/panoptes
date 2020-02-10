package org.slaq.slaqworx.panoptes.ui.trading;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import org.slaq.slaqworx.panoptes.ui.ComponentUtil;
import org.slaq.slaqworx.panoptes.ui.MinMaxField;

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
     *
     * @param securityProvider
     *            the {@code SecurityDataProvider} to use to query {@code Security} data
     */
    public SecurityFilterPanel(SecurityDataProvider securityProvider) {
        setResponsiveSteps(new ResponsiveStep("1em", NUM_COLUMNS));

        TextField assetIdTextField = ComponentUtil.createTextField("Asset ID");
        add(assetIdTextField);
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
            securityProvider.setFilter(e -> true);
        });
        Button reset = ComponentUtil.createButton("Reset", event -> {
            // clear the value of every child element that has a value
            getElement().getChildren().forEach(e -> e.getComponent().ifPresent(c -> {
                if (c instanceof HasValue) {
                    ((HasValue<?, ?>)c).clear();
                } else if (c instanceof MinMaxField) {
                    ((MinMaxField<?>)c).clear();
                }
            }));
        });

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(filter, reset);
        filter.getStyle().set("margin-right", "0.3em");

        add(actions, NUM_COLUMNS);
    }
}
