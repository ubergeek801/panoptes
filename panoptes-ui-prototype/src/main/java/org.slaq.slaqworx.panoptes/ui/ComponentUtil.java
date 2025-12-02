package org.slaq.slaqworx.panoptes.ui;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

/**
 * Provides utilities for managing UI components with consistency.
 *
 * @author jeremy
 */
public class ComponentUtil {
  /** Creates a new {@link ComponentUtil}. Restricted to enforce class utility semantics. */
  protected ComponentUtil() {
    // nothing to do
  }

  /**
   * Creates a new {@link Button}.
   *
   * @param labelText the text to be used as the button label
   * @return a {@link Button}
   */
  public static Button createButton(String labelText) {
    return createButton(labelText, null);
  }

  /**
   * Creates a new {@link Button}.
   *
   * @param labelText the text to be used as the button label
   * @param clickListener the listener to be invoked when the button is clicked
   * @return a {@link Button}
   */
  public static Button createButton(
      String labelText, ComponentEventListener<ClickEvent<Button>> clickListener) {
    Button button = new Button(labelText);
    if (clickListener != null) {
      button.addClickListener(clickListener);
    }
    button.addThemeVariants(ButtonVariant.LUMO_SMALL);

    return button;
  }

  /**
   * Creates a new {@link DatePicker} with no date initially populated.
   *
   * @param labelText the text to be used to label the component
   * @return a {@link DatePicker}
   */
  public static DatePicker createDatePicker(String labelText) {
    return createDatePicker(labelText, null);
  }

  /**
   * Creates a new {@link DatePicker} with no date initially populated.
   *
   * @param labelText the text to be used to label the component
   * @param placeholderText the placeholder text to appear in the date field
   * @return a {@link DatePicker}
   */
  public static DatePicker createDatePicker(String labelText, String placeholderText) {
    return createDatePicker(labelText, placeholderText, null);
  }

  /**
   * Creates a new {@link DatePicker} with an initially populated date.
   *
   * @param labelText the text to be used to label the component
   * @param placeholderText the placeholder text to appear in the date field
   * @param initialDate the date to initially appear in the date picker
   * @return a {@link DatePicker}
   */
  public static DatePicker createDatePicker(
      String labelText, String placeholderText, LocalDate initialDate) {
    DatePicker datePicker = new DatePicker(labelText, initialDate);
    datePicker.setPlaceholder(placeholderText);
    datePicker.getElement().getThemeList().add(TextFieldVariant.LUMO_SMALL.getVariantName());

    return datePicker;
  }

  /**
   * Creates a new {@link Div}.
   *
   * @param divText the text of the Div
   * @return a {@link Div}
   */
  public static Div createDiv(String divText) {
    Div div = new Div(divText);
    div.getStyle().set("font-size", "80%");

    return div;
  }

  /**
   * Creates a new {@link MinMaxField} for setting dates.
   *
   * @param labelText the label to be applied to the layout
   * @return a {@link MinMaxField}
   */
  public static MinMaxField<LocalDate> createMinMaxDateField(String labelText) {
    DatePicker minDate = createDatePicker(null, "Min");
    DatePicker maxDate = createDatePicker(null, "Max");

    return new MinMaxField<>(labelText, minDate, maxDate);
  }

  /**
   * Creates a new {@link MinMaxField} for setting numeric values.
   *
   * @param labelText the name of the attribute for which the values apply
   * @return a {@link MinMaxField}
   */
  public static MinMaxField<BigDecimal> createMinMaxNumberField(String labelText) {
    BigDecimalField minField = createNumberField("Min");
    BigDecimalField maxField = createNumberField("Max");

    return new MinMaxField<>(labelText, minField, maxField);
  }

  /**
   * Creates a new {@link BigDecimalField}.
   *
   * @param placeholderText the placeholder text to appear in the number field
   * @return a {@link BigDecimalField}
   */
  public static BigDecimalField createNumberField(String placeholderText) {
    BigDecimalField numberField = new BigDecimalField();
    numberField.setPlaceholder(placeholderText);
    numberField.setClearButtonVisible(true);
    numberField.addThemeVariants(TextFieldVariant.LUMO_SMALL, TextFieldVariant.LUMO_ALIGN_RIGHT);

    return numberField;
  }

  /**
   * Creates a new {@link Select}.
   *
   * @param placeholderText the placeholder text to appear in the select field
   * @param isEmptySelectionAllowed {@code true} if the selection is allowed to be empty, {@code
   *     false} otherwise
   * @param items the items to be populated in the select
   * @return a {@link Select}
   */
  public static Select<String> createSelect(
      String placeholderText, boolean isEmptySelectionAllowed, Collection<String> items) {
    Select<String> select = new Select<>();
    select.setPlaceholder(placeholderText);
    select.setItems(items);
    select.setEmptySelectionAllowed(isEmptySelectionAllowed);
    select.getElement().getThemeList().add(TextFieldVariant.LUMO_SMALL.getVariantName());

    return select;
  }

  /**
   * Creates a new {@link Select}.
   *
   * @param placeholderText the placeholder text to appear in the select field
   * @param isEmptySelectionAllowed {@code true} if the selection is allowed to be empty, {@code
   *     false} otherwise
   * @param items the items to be populated in the select
   * @return a {@link Select}
   */
  public static Select<String> createSelect(
      String placeholderText, boolean isEmptySelectionAllowed, String... items) {
    return createSelect(placeholderText, isEmptySelectionAllowed, Arrays.asList(items));
  }

  /**
   * Creates a new {@link TextField}.
   *
   * @param placeholderText the placeholder text to appear in the text field
   * @return a {@link TextField}
   */
  public static TextField createTextField(String placeholderText) {
    TextField textField = new TextField();
    textField.setPlaceholder(placeholderText);
    textField.setClearButtonVisible(true);
    textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);

    return textField;
  }
}
