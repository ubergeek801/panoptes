package org.slaq.slaqworx.panoptes.util;

import com.hazelcast.aggregation.Aggregator;
import java.io.Serial;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * A Hazelcast {@link Aggregator} which extracts the distinct non-{@code null} values of a specified
 * {@link SecurityAttribute}, as well as a solid entry in the Impractically Long Class Names
 * Competition.
 *
 * @param <T> the value type of the {@link Security} attribute being aggregated
 * @author jeremy
 */
public class DistinctSecurityAttributeValuesAggregator<T extends Comparable<T>>
    implements Aggregator<Map.Entry<SecurityKey, Security>, SortedSet<T>> {
  @Serial private static final long serialVersionUID = 1L;

  // we use a String rather than the SecurityAttribute itself to simplify serialization
  @Nonnull private final String attributeName;
  @Nonnull private final TreeSet<T> distinctValues = new TreeSet<>();

  /**
   * Creates a new {@link DistinctSecurityAttributeValuesAggregator} that aggregates distinct values
   * of the given {@link SecurityAttribute}.
   *
   * @param attribute the {@link SecurityAttribute} for which to aggregate distinct values
   */
  public DistinctSecurityAttributeValuesAggregator(@Nonnull SecurityAttribute<T> attribute) {
    attributeName = attribute.getName();
  }

  @Override
  public void accumulate(Entry<SecurityKey, Security> input) {
    T value = (T) input.getValue().getAttributeValue(SecurityAttribute.of(attributeName), false);
    if (value != null) {
      distinctValues.add(value);
    }
  }

  @Override
  @Nonnull
  public SortedSet<T> aggregate() {
    return distinctValues;
  }

  @Override
  public void combine(Aggregator aggregator) {
    Aggregator<Map.Entry<SecurityKey, Security>, SortedSet<T>> otherAggregator = aggregator;
    distinctValues.addAll(otherAggregator.aggregate());
  }
}
