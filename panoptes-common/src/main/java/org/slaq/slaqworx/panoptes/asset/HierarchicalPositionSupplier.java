package org.slaq.slaqworx.panoptes.asset;

import java.util.EnumSet;
import java.util.stream.Stream;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.trade.TaxLot;

/**
 * A {@link PositionSupplier} which provides hierarchies of {@link Position}s, such as by employing
 * "look-through" to {@link Position}s of constituent {@link Portfolio}s, or by providing visibility
 * to a {@link Position}'s individual {@link TaxLot}s.
 *
 * @author jeremy
 */
public interface HierarchicalPositionSupplier extends PositionSupplier {
  /**
   * Obtains this {@link PositionSupplier}'s {@link Position}s as a (new) {@link Stream}, applying
   * the given hierarchy options.
   *
   * @param positionHierarchyOptions
   *     the (possibly empty) hierarchy options to be applied
   * @param evaluationContext
   *     the {@link EvaluationContext} in which the {@link Position}s are being obtained
   *
   * @return a {@link Stream} of {@link Position}s
   */
  default Stream<? extends Position> getPositions(
      EnumSet<PositionHierarchyOption> positionHierarchyOptions,
      EvaluationContext evaluationContext) {
    Stream<? extends Position> positionStream = getPositions();

    if (positionHierarchyOptions.contains(PositionHierarchyOption.LOOKTHROUGH)) {
      positionStream = positionStream.flatMap(p -> p.getLookthroughPositions(evaluationContext));
    }

    if (positionHierarchyOptions.contains(PositionHierarchyOption.TAXLOT)) {
      positionStream = positionStream.flatMap(Position::getTaxLots);
    }

    return positionStream;
  }

  enum PositionHierarchyOption {
    LOOKTHROUGH, TAXLOT
  }
}
