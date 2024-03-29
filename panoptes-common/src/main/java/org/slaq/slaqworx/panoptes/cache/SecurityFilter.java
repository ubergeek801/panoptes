package org.slaq.slaqworx.panoptes.cache;

import com.hazelcast.query.Predicate;
import java.io.Serial;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityAttributes;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.util.SerializablePredicate;

/**
 * A Hazelcast {@link Predicate} that supports querying {@link Security} entities based on various
 * attribute values.
 *
 * @author jeremy
 */
public class SecurityFilter implements Predicate<SecurityKey, Security> {
  @Serial private static final long serialVersionUID = 1L;

  private SerializablePredicate<SecurityAttributes> predicate;

  /** Creates a new, empty {@link SecurityFilter}. */
  public SecurityFilter() {
    // nothing to do
  }

  /**
   * Adds a filter on the given {@link SecurityAttribute} having the given value.
   *
   * @param attribute the {@link SecurityAttribute} on which to filter
   * @param value the attribute value to be included in results
   * @return this {@link SecurityFilter} instance
   */
  public SecurityFilter add(@Nonnull SecurityAttribute<String> attribute, String value) {
    if (value == null || value.isBlank()) {
      // nothing new to add
      return this;
    }

    SerializablePredicate<SecurityAttributes> attributeFilter =
        (a -> {
          String attributeValue = a.getValue(attribute);
          if (attributeValue != null) {
            return attributeValue.equals(value);
          }

          return false;
        });

    return add(attributeFilter);
  }

  /**
   * Adds a filter on the given {@link SecurityAttribute} having a value in the given range.
   *
   * @param attribute the {@link SecurityAttribute} on which to filter
   * @param minValue the minimum attribute value to be included in results
   * @param maxValue the maximum attribute value to be included in results
   * @return this {@link SecurityFilter} instance
   */
  public <T extends Comparable<? super T>> SecurityFilter add(
      @Nonnull SecurityAttribute<T> attribute, T minValue, T maxValue) {
    if (minValue == null && maxValue == null) {
      // nothing new to add
      return this;
    }

    SerializablePredicate<SecurityAttributes> attributeFilter =
        (a -> {
          T attributeValue = a.getValue(attribute);
          if (attributeValue != null) {
            boolean isMinValueMet = (minValue == null || attributeValue.compareTo(minValue) >= 0);
            boolean isMaxValueMet = (maxValue == null || attributeValue.compareTo(maxValue) <= 0);

            return isMinValueMet && isMaxValueMet;
          }

          return false;
        });

    return add(attributeFilter);
  }

  /**
   * Adds a filter matching the given {@link SecurityAttributes} predicate.
   *
   * @param p the predicate to be matched
   * @return this {@link SecurityFilter} instance
   */
  public SecurityFilter add(@Nonnull SerializablePredicate<SecurityAttributes> p) {
    if (predicate == null) {
      predicate = p;
    }

    predicate = predicate.and(p);

    return this;
  }

  @Override
  public boolean apply(@Nonnull Entry<SecurityKey, Security> mapEntry) {
    return (predicate == null || predicate.test(mapEntry.getValue().getAttributes()));
  }
}
