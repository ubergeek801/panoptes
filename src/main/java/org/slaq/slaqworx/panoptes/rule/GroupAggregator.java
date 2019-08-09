package org.slaq.slaqworx.panoptes.rule;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * GroupAggregator is the interface for functions which aggregate existing groups of positions into
 * new groups.
 *
 * @author jeremy
 * @param <K>
 *            the EvaluationGroup key on which the function operates
 */
@FunctionalInterface
public interface GroupAggregator<K extends Serializable> {
    /**
     * Aggregates the given Position classifications into zero or more new classifications. For
     * example, a GroupAggregator may select Positions held in the top five issuers and create a new
     * "top 5 issuer" EvaluationGroup consisting of those Positions.
     *
     * @param classifiedPositions
     *            the Positions already classified
     * @return a Map relating zero or more new EvaluationGroups to their constituent Positions
     */
    public Map<EvaluationGroup<K>, Collection<Position>>
            aggregate(Map<EvaluationGroup<?>, Collection<Position>> classifiedPositions);
}
