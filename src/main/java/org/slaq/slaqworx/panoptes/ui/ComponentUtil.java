package org.slaq.slaqworx.panoptes.ui;

import java.time.LocalDate;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;

/**
 * {@code ComponentUtil} provides utilities for managing UI components with consistency.
 *
 * @author jeremy
 */
public class ComponentUtil {
    public static Button createButton(String labelText,
            ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(labelText);
        button.addClickListener(clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);

        return button;
    }

    public static TextField createDateField(String placeholderText) {
        TextField dateField = createTextField(placeholderText);
        // nothing else special about a date field right now

        return dateField;
    }

    public static DatePicker createDatePicker(String labelText) {
        return createDatePicker(labelText, null);
    }

    public static DatePicker createDatePicker(String labelText, LocalDate initialDate) {
        DatePicker datePicker = new DatePicker(labelText, initialDate);

        return datePicker;
    }

    public static Label createLabel(String labelText) {
        Label label = new Label(labelText);
        label.getStyle().set("font-size", "80%");

        return label;
    }

    public static HorizontalLayout createMinMaxDateField(String labelText) {
        Label label = createLabel(labelText);
        TextField minDate = createDateField("Min");
        TextField maxDate = createDateField("Max");

        return createMinMaxLayout(label, minDate, maxDate);
    }

    public static HorizontalLayout createMinMaxLayout(Label label, Component min, Component max) {
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

    public static HorizontalLayout createMinMaxNumberField(String labelText) {
        Label label = createLabel(labelText);
        NumberField minField = createNumberField("Min");
        NumberField maxField = createNumberField("Max");

        return createMinMaxLayout(label, minField, maxField);
    }

    public static NumberField createNumberField(String placeholderText) {
        NumberField numberField = new NumberField();
        numberField.setPlaceholder(placeholderText);
        // TODO hack to allow decimal values; clean up when Vaadin fixes their bug
        // https://github.com/vaadin/vaadin-text-field-flow/issues/173
        numberField.setStep(0.0001);
        numberField.setClearButtonVisible(true);
        numberField.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        return numberField;
    }

    public static Select<String> createSelect(String labelText, String... options) {
        Select<String> select = new Select<>(options);
        select.setLabel(labelText);

        return select;
    }

    public static TextField createTextField(String placeholderText) {
        TextField textField = new TextField();
        textField.setPlaceholder(placeholderText);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        return textField;
    }

    /**
     * Creates a new {@code ComponentUtil}. Restricted to enforce class utility semantics.
     */
    protected ComponentUtil() {
        // nothing to do
    }
}
