package org.slaq.slaqworx.panoptes.rule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

/**
 * {@code ValueProvider} is a {@code Function} that converts a value of a specified type to a
 * {@code Double}, to facilitate calculations on various types of {@code SecurityAttribute}s.
 *
 * @author jeremy
 * @param <T>
 *            the type that can be converted by the {@code ValueProvider}
 */
public interface ValueProvider<T> extends Function<T, Double> {
    /**
     * Produces a {@code ValueProvider} that converts from {@code BigDecimal}.
     *
     * @return a {@code ValueProvider} for converting {@code BigDecimal} values
     */
    public static ValueProvider<BigDecimal> forBigDecimal() {
        return v -> (v == null ? null : v.doubleValue());
    }

    /**
     * Produces a {@code ValueProvider} that converts from values of the given {@code Class}.
     *
     * @param <T>
     *            the class type of values to be converted
     * @param clazz
     *            the {@code Class} of values to be converted
     * @return a {@code ValueProvider} of the requested type, if available
     * @throws IllegalArgumentException
     *             if a {@code ValueProvider} is not available for the requested type
     */
    @SuppressWarnings("unchecked")
    public static <T> ValueProvider<T> forClass(Class<T> clazz) {
        if (Double.class.isAssignableFrom(clazz)) {
            return (ValueProvider<T>)forDouble();
        }
        if (BigDecimal.class.isAssignableFrom(clazz)) {
            return (ValueProvider<T>)forBigDecimal();
        }
        if (LocalDate.class.isAssignableFrom(clazz)) {
            return (ValueProvider<T>)forDaysUntilDate();
        }

        throw new IllegalArgumentException("unsupported ValueProvider class: " + clazz);
    }

    /**
     * Produces a {@code ValueProvider} that converts a {@code LocalDate} into the number of days
     * between the current date and that date.
     *
     * @return a {@code ValueProvider} for converting {@code LocalDate} values
     */
    public static ValueProvider<LocalDate> forDaysUntilDate() {
        // TODO come up with a reasonable way to get the effective current date
        return v -> (v == null ? null : (double)LocalDate.now().until(v, ChronoUnit.DAYS));
    }

    /**
     * Produces a {@code ValueProvider} that trivially "converts" a {@code Double}.
     *
     * @return a {@code ValueProvider} for handling {@code Double} values
     */
    public static ValueProvider<Double> forDouble() {
        return v -> v;
    }
}
