package org.slaq.slaqworx.panoptes.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Encapsulates a label and pair of input fields to specify a minimum and maximum value for a field
 * (e.g. to specify a filter range).
 *
 * @param <V> the value type of the contained fields
 * @author jeremy
 */
public class MinMaxField<V> extends CustomField<Pair<V, V>> {
  private static final long serialVersionUID = 1L;

  private final HasValue<?, V> min;
  private final HasValue<?, V> max;

  /**
   * Creates a {@link MinMaxField}, consisting of the given components, to display and/or input
   * minimum and maximum values for some attribute.
   *
   * @param labelText the name of the attribute for which the values apply
   * @param min a {@link Component} containing the minimum value
   * @param max a {@link Component} containing the maximum value
   */
  public MinMaxField(String labelText, Component min, Component max) {
    HorizontalLayout outerLayout = new HorizontalLayout();

    HasValue<?, V> minHasValue = (HasValue<?, V>) min;
    this.min = minHasValue;
    HasValue<?, V> maxHasValue = (HasValue<?, V>) max;
    this.max = maxHasValue;

    // unfortunately some components want to overflow their boundaries, so some hackery is
    // necessary to try to keep them in line
    HorizontalLayout innerLayout = new HorizontalLayout();
    innerLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
    innerLayout.setJustifyContentMode(JustifyContentMode.END);
    innerLayout.setMaxWidth("80%");
    if (min instanceof HasSize) {
      ((HasSize) min).setMaxWidth("50%");
    }
    if (max instanceof HasSize) {
      ((HasSize) max).setMaxWidth("50%");
    }
    innerLayout.addAndExpand(min, max);

    outerLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
    outerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
    outerLayout.add(ComponentUtil.createLabel(labelText));
    outerLayout.addAndExpand(innerLayout);

    add(outerLayout);
  }

  /** Clears the current input field values. */
  @Override
  public void clear() {
    min.clear();
    max.clear();
  }

  /**
   * Obtains the max value specified by the input field.
   *
   * @return the max value or {@code null} if it is not set
   */
  public V getMaxValue() {
    return max.getValue();
  }

  /**
   * Obtains the min value specified by the input field.
   *
   * @return the min value or {@code null} if it is not set
   */
  public V getMinValue() {
    return min.getValue();
  }

  @Override
  protected Pair<V, V> generateModelValue() {
    return Pair.of(getMinValue(), getMaxValue());
  }

  @Override
  protected void setPresentationValue(Pair<V, V> newValue) {
    if (newValue == null) {
      min.setValue(null);
      max.setValue(null);
    } else {
      min.setValue(newValue.getLeft());
      max.setValue(newValue.getRight());
    }
  }
}
