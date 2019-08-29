package org.slaq.slaqworx.panoptes.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * TopNSecurityAttributeAggregator is a SecurityAttributeGroupClassifier which classifies Positions
 * based on a specified SecurityAttribute, and also (as a GroupAggregator) aggregates the "top n"
 * resulting groups into a composite group.
 *
 * @author jeremy
 */
public class TopNSecurityAttributeAggregator extends SecurityAttributeGroupClassifier
        implements GroupAggregator<SecurityAttribute<?>> {
    static class Configuration {
        public String attribute;
        public int count;
    }

    /**
     * Creates a new TopNSecurityAttributeAggregator which aggregates Positions on the
     * SecurityAttribute specified in the JSON configuration.
     *
     * @param jsonConfiguration
     *            a JSON configuration specifying the SecurityAttribute on which to aggregate
     *            Positions
     */
    public static TopNSecurityAttributeAggregator fromJson(String jsonConfiguration) {
        Configuration configuration;
        try {
            configuration = JsonConfigurable.defaultObjectMapper().readValue(jsonConfiguration,
                    Configuration.class);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not parse JSON configuration " + jsonConfiguration,
                    e);
        }

        return new TopNSecurityAttributeAggregator(SecurityAttribute.of(configuration.attribute),
                configuration.count);
    }

    private final int count;

    /**
     * Creates a new TopNSecurityAttributeAggregator which aggregates Positions on the given
     * SecurityAttribute.
     *
     * @param securityAttribute
     *            the SecurityAttribute on which to aggregate Positions
     * @param count
     *            the number of groups to collect into the "top n" metagroup
     */
    public TopNSecurityAttributeAggregator(SecurityAttribute<?> securityAttribute, int count) {
        super(securityAttribute);
        this.count = count;
    }

    @Override
    public Map<EvaluationGroup<SecurityAttribute<?>>, Collection<Position>>
            aggregate(Map<EvaluationGroup<?>, Collection<Position>> classifiedPositions) {
        ArrayList<Position> aggregatePositions = new ArrayList<>();
        // if we already have fewer groups than the desired, then just collect it all
        if (classifiedPositions.size() <= count) {
            classifiedPositions.values().forEach(positions -> aggregatePositions.addAll(positions));
        } else {
            // create a list of PositionSuppliers and sort it by total amount, descending
            ArrayList<PositionSupplier> sortedClassifiedPositions =
                    new ArrayList<>(classifiedPositions.size());
            classifiedPositions.forEach((g, positions) -> {
                sortedClassifiedPositions.add(new PositionSet(positions));
            });
            Collections.sort(sortedClassifiedPositions,
                    (s1, s2) -> Double.compare(s2.getTotalMarketValue(), s1.getTotalMarketValue()));

            // collect the first "count" PositionSuppliers into a single supplier
            for (int i = 0; i < count; i++) {
                aggregatePositions.addAll(sortedClassifiedPositions.get(i).getPositions()
                        .collect(Collectors.toList()));
            }
        }

        return Map.of(
                new EvaluationGroup<SecurityAttribute<?>>(
                        getSecurityAttribute() + ".top(" + count + ")", getSecurityAttribute()),
                aggregatePositions);
    }

    @Override
    public String getJsonConfiguration() {
        Configuration configuration = new Configuration();
        configuration.attribute = getSecurityAttribute().getName();
        configuration.count = count;

        try {
            return JsonConfigurable.defaultObjectMapper().writeValueAsString(configuration);
        } catch (JsonProcessingException e) {
            // TODO throw a better exception
            throw new RuntimeException("could not serialize JSON configuration", e);
        }
    }
}
