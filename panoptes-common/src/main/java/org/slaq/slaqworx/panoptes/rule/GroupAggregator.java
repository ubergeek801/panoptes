package org.slaq.slaqworx.panoptes.rule;

import java.util.Map;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;

/**
 * The interface for functions which aggregate existing groups of positions into new groups.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface GroupAggregator {
  /**
   * Aggregates the given {@link Position} classifications into zero or more new classifications.
   * For example, a {@link GroupAggregator} may select {@link Position}s held in the top five
   * issuers and create a new "top 5 issuer" {@link EvaluationGroup} consisting of those {@link
   * Position}s.
   *
   * @param classifiedPositions the {@link Position}s already classified
   * @param evaluationContext the {@link EvaluationContext} in which to perform the evaluation
   * @return a {@link Map} consisting of the existing classified {@link Position}s (possibly
   *     filtered) while adding zero or more new mappings of {@link EvaluationGroup}s to their
   *     constituent {@link Position}s
   */
  @Nonnull
  Map<EvaluationGroup, PositionSupplier> aggregate(
      @Nonnull Map<EvaluationGroup, PositionSupplier> classifiedPositions,
      @Nonnull EvaluationContext evaluationContext);
}
