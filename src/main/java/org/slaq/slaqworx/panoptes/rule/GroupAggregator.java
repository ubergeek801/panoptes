package org.slaq.slaqworx.panoptes.rule;

import java.util.Collection;
import java.util.Map;

import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * GroupAggregator is the interface for functions which aggregate existing groups of positions into
 * new groups.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface GroupAggregator {
    /**
     * Aggregates the given Position classifications into zero or more new classifications. For
     * example, a GroupAggregator may select Positions held in the top five issuers and create a new
     * "top 5 issuer" EvaluationGroup consisting of those Positions.
     *
     * @param classifiedPositions
     *            the Positions already classified
     * @return a Map relating zero or more new EvaluationGroups to their constituent Positions
     */
    public Map<EvaluationGroup, Collection<Position>>
            aggregate(Map<EvaluationGroup, Collection<Position>> classifiedPositions);
}
