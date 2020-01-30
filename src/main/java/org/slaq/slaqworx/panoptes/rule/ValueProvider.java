package org.slaq.slaqworx.panoptes.rule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

public interface ValueProvider<T> extends Function<T, Double> {
    public static ValueProvider<BigDecimal> forBigDecimal() {
        return v -> (v == null ? null : v.doubleValue());
    }

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

    public static ValueProvider<LocalDate> forDaysUntilDate() {
        // TODO come up with a reasonable way to get the effective current date
        return v -> (v == null ? null : (double)LocalDate.now().until(v, ChronoUnit.DAYS));
    }

    public static ValueProvider<Double> forDouble() {
        return v -> v;
    }
}
