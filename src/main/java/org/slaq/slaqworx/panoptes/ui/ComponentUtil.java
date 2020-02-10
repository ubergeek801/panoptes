package org.slaq.slaqworx.panoptes.ui;

import java.time.LocalDate;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Label;
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
    /**
     * Creates a new {@code Button}.
     *
     * @param labelText
     *            the text to be used as the button label
     * @param clickListener
     *            the listener to be invoked when the button is clicked
     * @return a {@code Button}
     */
    public static Button createButton(String labelText) {
        return createButton(labelText, null);
    }

    /**
     * Creates a new {@code Button}.
     *
     * @param labelText
     *            the text to be used as the button label
     * @param clickListener
     *            the listener to be invoked when the button is clicked
     * @return a {@code Button}
     */
    public static Button createButton(String labelText,
            ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(labelText);
        if (clickListener != null) {
            button.addClickListener(clickListener);
        }
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);

        return button;
    }

    /**
     * Creates a new {@code TextField}.
     *
     * @param placeholderText
     *            the placeholder text to appear in the text field
     * @return a {@code TextField}
     */
    public static TextField createDateField(String placeholderText) {
        TextField dateField = createTextField(placeholderText);
        // nothing else special about a date field right now

        return dateField;
    }

    /**
     * Creates a new {@code DatePicker} with no date initially populated.
     *
     * @param labelText
     *            the text to be used to label the component
     * @return a {@code DatePicker}
     */
    public static DatePicker createDatePicker(String labelText) {
        return createDatePicker(labelText, null);
    }

    /**
     * Creates a new {@code DatePicker} with an initially populated date.
     *
     * @param labelText
     *            the text to be used to label the component
     * @param initialDate
     *            the date to initially appear in the date picker
     * @return a {@code DatePicker}
     */
    public static DatePicker createDatePicker(String labelText, LocalDate initialDate) {
        DatePicker datePicker = new DatePicker(labelText, initialDate);

        return datePicker;
    }

    /**
     * Creates a new {@code Label}.
     *
     * @param labelText
     *            the text of the label
     * @return a {@code Label}
     */
    public static Label createLabel(String labelText) {
        Label label = new Label(labelText);
        label.getStyle().set("font-size", "80%");

        return label;
    }

    /**
     * Creates a new {@code MinMaxField} for setting dates.
     *
     * @param labelText
     *            the label to be applied to the layout
     * @return a {@code MinMaxField}
     */
    public static MinMaxField<String> createMinMaxDateField(String labelText) {
        TextField minDate = createDateField("Min");
        TextField maxDate = createDateField("Max");

        return new MinMaxField<>(labelText, minDate, maxDate);
    }

    /**
     * Creates a new {@code MinMaxField} for setting numeric values.
     *
     * @param labelText
     *            the name of the attribute for which the values apply
     * @return a {@code MinMaxField}
     */
    public static MinMaxField<Double> createMinMaxNumberField(String labelText) {
        NumberField minField = createNumberField("Min");
        NumberField maxField = createNumberField("Max");

        return new MinMaxField<>(labelText, minField, maxField);
    }

    /**
     * Creates a new {@code NumberField}.
     *
     * @param placeholderText
     *            the placeholder text to appear in the number field
     * @return a {@code NumberField}
     */
    public static NumberField createNumberField(String placeholderText) {
        NumberField numberField = new NumberField();
        numberField.setPlaceholder(placeholderText);
        numberField.setClearButtonVisible(true);
        numberField.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        return numberField;
    }

    /**
     * Creates a new {@code Select}.
     *
     * @param labelText
     *            the text to be used as the select label
     * @param options
     *            the options to be populated in the select
     * @return a {@code Select}
     */
    public static Select<String> createSelect(String labelText, String... options) {
        Select<String> select = new Select<>(options);
        select.setLabel(labelText);

        return select;
    }

    /**
     * Creates a new {@code TextField}.
     *
     * @param placeholderText
     *            the placeholder text to appear in the text field
     * @return a {@code TextField}
     */
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
