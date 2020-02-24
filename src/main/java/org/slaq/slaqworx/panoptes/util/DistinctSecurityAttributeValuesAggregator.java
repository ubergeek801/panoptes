package org.slaq.slaqworx.panoptes.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hazelcast.aggregation.Aggregator;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * {@code DistinctSecurityAttributeValuesAggregator}, in addition to being a solid entry in the
 * Impractically Long Class Names Competition, is an {@code Aggregator} that extracts the distinct
 * non-{@code null} values of a specified {@code SecurityAttribute}.
 *
 * @author jeremy
 * @param <T>
 *            the value type of the {@code Security} attribute being aggregated
 */
public class DistinctSecurityAttributeValuesAggregator<T>
        implements Aggregator<Map.Entry<SecurityKey, Security>, SortedSet<T>> {
    private static final long serialVersionUID = 1L;

    // we use a String rather than the SecurityAttribute itself to simplify serialization
    private final String attributeName;
    private final TreeSet<T> distinctValues = new TreeSet<>();

    /**
     * Creates a new {@code DistinctSecurityAttributeValuesAggregator} that aggregates distinct
     * values of the given {@code SecurityAttribute}.
     *
     * @param attribute
     *            the {@code SecurityAttribute} for which to aggregate distinct values
     */
    public DistinctSecurityAttributeValuesAggregator(SecurityAttribute<T> attribute) {
        this.attributeName = attribute.getName();
    }

    @Override
    public void accumulate(Entry<SecurityKey, Security> input) {
        @SuppressWarnings("unchecked")
        T value = (T)input.getValue().getAttributeValue(SecurityAttribute.of(attributeName), false);
        if (value != null) {
            distinctValues.add(value);
        }
    }

    @Override
    public SortedSet<T> aggregate() {
        return distinctValues;
    }

    @Override
    public void combine(Aggregator aggregator) {
        Aggregator<Map.Entry<SecurityKey, Security>, SortedSet<T>> otherAggregator = aggregator;
        distinctValues.addAll(otherAggregator.aggregate());
    }
}
