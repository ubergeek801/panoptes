package org.slaq.slaqworx.panoptes.rule;

import java.util.Map;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;

/**
 * The interface for functions which aggregate existing groups of positions into new groups.
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
     * @param evaluationContext
     *            the {@code EvaluationContext} in which to perform the evaluation
     * @return a {@code Map} consisting of the existing classified {@code Position}s (possibly
     *         filtered) while adding zero or more new mappings of {@code EvaluationGroup}s to their
     *         constituent {@code Position}s
     */
    public Map<EvaluationGroup, PositionSupplier> aggregate(
            Map<EvaluationGroup, PositionSupplier> classifiedPositions,
            EvaluationContext evaluationContext);
}
