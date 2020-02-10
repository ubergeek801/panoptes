package org.slaq.slaqworx.panoptes.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * {@code MinMaxField} encapsulates a label and pair of input fields to specify a minimum and
 * maximum value for a field (e.g to specify a filter range).
 *
 * @author jeremy
 * @param <V>
 *            the value type of the contained fields
 */
public class MinMaxField<V> extends HorizontalLayout {
    private static final long serialVersionUID = 1L;

    private final HasValue<?, V> min;
    private final HasValue<?, V> max;

    /**
     * Creates a {@code MinMaxField}, consisting of the given components, to display and/or input
     * minimum and maximum values for some attribute.
     *
     * @param labelText
     *            the name of the attribute for which the values apply
     * @param min
     *            a {@code Component} containing the minimum value
     * @param max
     *            a {@code Component} containing the maximum value
     */
    public MinMaxField(String labelText, Component min, Component max) {
        @SuppressWarnings("unchecked")
        HasValue<?, V> minHasValue = (HasValue<?, V>)min;
        this.min = minHasValue;
        @SuppressWarnings("unchecked")
        HasValue<?, V> maxHasValue = (HasValue<?, V>)max;
        this.max = maxHasValue;

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

        setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(ComponentUtil.createLabel(labelText));
        addAndExpand(innerLayout);
    }

    /**
     * Clears the current input field values.
     */
    public void clear() {
        clear(min);
        clear(max);
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

    /**
     * Clears the current input field value on the specified component.
     *
     * @param component
     *            the component on which to clear the value
     */
    protected void clear(HasValue<?, V> component) {
        // FIXME for some reason this doesn't work on NumberFields if they have an invalid value
        // (https://github.com/vaadin/vaadin-text-field/issues/414 may be related)
        component.clear();
    }
}
