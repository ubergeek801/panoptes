package org.slaq.slaqworx.panoptes.rule;

import java.util.Collection;
import java.util.Map;

import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * {@code GroupAggregator} is the interface for functions which aggregate existing groups of
 * positions into new groups.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface GroupAggregator {
    /**
     * Aggregates the given {@code Position} classifications into zero or more new classifications.
     * For example, a {@code GroupAggregator} may select {@code Position}s held in the top five
     * issuers and create a new "top 5 issuer" {@code EvaluationGroup} consisting of those
     * {@code Position}s.
     *
     * @param classifiedPositions
     *            the {@code Position}s already classified
     * @return a {@code Map} relating zero or more new {@code EvaluationGroup}s to their constituent
     *         {@code Position}s
     */
    public Map<EvaluationGroup, Collection<Position>>
            aggregate(Map<EvaluationGroup, Collection<Position>> classifiedPositions);
}
