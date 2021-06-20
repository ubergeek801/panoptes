package org.slaq.slaqworx.panoptes.rule;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.RatingNotch;
import org.slaq.slaqworx.panoptes.asset.RatingScale;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * A {@link BiFunction} that converts a value of a specified type (within a given {@link
 * EvaluationContext}) to a {@link Double}, to facilitate calculations on various types of {@link
 * SecurityAttribute}s. Implements {@link Serializable} for convenience of implementing
 * cluster-friendly {@link Security} filters.
 *
 * @param <T>
 *     the type that can be converted by the {@link ValueProvider}
 *
 * @author jeremy
 */
public interface ValueProvider<T> extends BiFunction<T, EvaluationContext, Double>, Serializable {
  /**
   * Produces a {@link ValueProvider} that converts from {@link BigDecimal}.
   *
   * @return a {@link ValueProvider} for converting {@link BigDecimal} values
   */
  @Nonnull
  static ValueProvider<BigDecimal> forBigDecimal() {
    return (v, c) -> (v == null ? null : v.doubleValue());
  }

  /**
   * Produces a {@link ValueProvider} that converts from values of the given {@link Class}.
   *
   * @param <T>
   *     the class type of values to be converted
   * @param clazz
   *     the {@link Class} of values to be converted
   *
   * @return a {@link ValueProvider} of the requested type, if available
   *
   * @throws IllegalArgumentException
   *     if a {@link ValueProvider} is not available for the requested type
   */
  @Nonnull
  static <T> ValueProvider<T> forClass(Class<T> clazz) {
    ValueProvider<T> provider = forClassIfAvailable(clazz);
    if (provider != null) {
      return provider;
    }

    throw new IllegalArgumentException("unsupported ValueProvider class: " + clazz);
  }

  /**
   * Produces, if available, a {@link ValueProvider} that converts from values of the given {@link
   * Class}.
   *
   * @param <T>
   *     the class type of values to be converted
   * @param clazz
   *     the {@link Class} of values to be converted
   *
   * @return a {@link ValueProvider} of the requested type, or {@code null} if not available
   */
  static <T> ValueProvider<T> forClassIfAvailable(Class<T> clazz) {
    if (Double.class.isAssignableFrom(clazz)) {
      return (ValueProvider<T>) forDouble();
    }
    if (BigDecimal.class.isAssignableFrom(clazz)) {
      return (ValueProvider<T>) forBigDecimal();
    }
    if (LocalDate.class.isAssignableFrom(clazz)) {
      // currently this is the only thing we know how to do with dates
      return (ValueProvider<T>) forDaysUntilDate();
    }
    if (String.class.isAssignableFrom(clazz)) {
      // currently this is the only thing we know how to do with strings
      return (ValueProvider<T>) forRatingSymbol();
    }

    return null;
  }

  /**
   * Produces a {@link ValueProvider} that converts a {@link LocalDate} into the number of days
   * between the effective current date (as supplied by the {@link EvaluationContext}) and that
   * date.
   *
   * @return a {@link ValueProvider} for converting {@link LocalDate} values
   */
  @Nonnull
  static ValueProvider<LocalDate> forDaysUntilDate() {
    // TODO get the effective current date from the EvaluationContext
    return (v, c) -> (v == null ? null : (double) LocalDate.now().until(v, ChronoUnit.DAYS));
  }

  /**
   * Produces a {@link ValueProvider} that trivially "converts" a {@link Double}.
   *
   * @return a {@link ValueProvider} for handling {@link Double} values
   */
  @Nonnull
  static ValueProvider<Double> forDouble() {
    return (v, c) -> v;
  }

  /**
   * Produces a {@link ValueProvider} that converts a rating symbol to the ordinal of its
   * corresponding {@link RatingNotch}.
   *
   * @return a {@link ValueProvider} for handling rating symbol values
   */
  static ValueProvider<String> forRatingSymbol() {
    // TODO support other RatingScales
    return (v, c) -> (v == null ? null :
        (double) RatingScale.defaultScale().getRatingNotch(v).getOrdinal());
  }
}
