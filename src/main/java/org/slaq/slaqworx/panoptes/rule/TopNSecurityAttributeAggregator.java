package org.slaq.slaqworx.panoptes.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * TopNSecurityAttributeAggregator is a SecurityAttributeGroupClassifier which classifies Positions
 * based on a specified SecurityAttribute, and also (as a GroupAggregator) aggregates the "top n"
 * resulting groups into a composite group.
 *
 * @author jeremy
 */
public class TopNSecurityAttributeAggregator extends SecurityAttributeGroupClassifier
        implements GroupAggregator<SecurityAttribute<?>> {
    private static final long serialVersionUID = 1L;

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
                    (s1, s2) -> Double.compare(s2.getTotalAmount(), s1.getTotalAmount()));

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
}
