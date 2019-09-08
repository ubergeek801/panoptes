package org.slaq.slaqworx.panoptes.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;

public class SecurityFilterPanel extends FormLayout {
    private static final long serialVersionUID = 1L;

    private static final int NUM_COLUMNS = 7; // TODO this isn't very "responsive"

    public SecurityFilterPanel() {
        setResponsiveSteps(new ResponsiveStep("1em", NUM_COLUMNS));

        add(createTextField("Asset ID"));
        add(createTextField("CUSIP"));
        add(createTextField("Description"), 2);
        add(createTextField("Country"));
        add(createTextField("Region"));
        add(createTextField("Sector"));
        add(createTextField("Currency"));
        add(createMinMaxNumberField("Coupon"), 2);
        add(createMinMaxDateField("Maturity"), 2);
        add(createMinMaxNumberField("Rating"), 2);
        add(createMinMaxNumberField("Yield"), 2);
        add(createMinMaxNumberField("Duration"), 2);
        add(createTextField("Issuer"));
        add(createMinMaxNumberField("Price"), 2);

        Button filter = new Button("Filter");
        filter.addThemeVariants(ButtonVariant.LUMO_SMALL);
        Button reset = new Button("Reset");
        reset.addThemeVariants(ButtonVariant.LUMO_SMALL);
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(filter, reset);
        filter.getStyle().set("margin-right", "0.3em");
        add(actions, NUM_COLUMNS);
    }

    protected TextField createDateField(String placeholderText) {
        TextField dateField = createTextField(placeholderText);
        // nothing else special about a date field right now

        return dateField;
    }

    protected Label createLabel(String labelText) {
        Label label = new Label(labelText);
        label.getStyle().set("font-size", "80%");

        return label;
    }

    protected HorizontalLayout createMinMaxDateField(String labelText) {
        Label label = createLabel(labelText);
        TextField minDate = createDateField("Min");
        TextField maxDate = createDateField("Max");

        return createMinMaxLayout(label, minDate, maxDate);
    }

    protected HorizontalLayout createMinMaxLayout(Label label, Component min, Component max) {
        // unfortunately some components want to overflow their boundaries, so some hackery is
        // necessary to try to keep them in line
        HorizontalLayout innerLayout = new HorizontalLayout();
        innerLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        innerLayout.setJustifyContentMode(JustifyContentMode.END);
        innerLayout.setMaxWidth("80%");
        if (min instanceof HasSize) {
            ((HasSize)min).setMaxWidth("50%");
        }
        if (max instanceof HasSize) {
            ((HasSize)max).setMaxWidth("50%");
        }
        innerLayout.addAndExpand(min, max);

        HorizontalLayout outerLayout = new HorizontalLayout();
        outerLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        outerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        outerLayout.add(label);
        outerLayout.addAndExpand(innerLayout);

        return outerLayout;
    }

    protected HorizontalLayout createMinMaxNumberField(String labelText) {
        Label label = createLabel(labelText);
        NumberField minField = createNumberField("Min");
        NumberField maxField = createNumberField("Max");

        return createMinMaxLayout(label, minField, maxField);
    }

    protected NumberField createNumberField(String placeholderText) {
        NumberField numberField = new NumberField();
        numberField.setPlaceholder(placeholderText);
        numberField.setClearButtonVisible(true);
        numberField.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        return numberField;
    }

    protected TextField createTextField(String placeholderText) {
        TextField textField = new TextField();
        textField.setPlaceholder(placeholderText);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        return textField;
    }
}
