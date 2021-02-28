package org.slaq.slaqworx.panoptes.asset;

import java.util.stream.Stream;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;

/**
 * Supplies {@link Position}s. An implementor might be a customer {@link Portfolio} or a "raw" set
 * of {@link Position}s. A {@link PositionSupplier} may provide access to a related {@link
 * Portfolio} (which may be the supplier itself), but note that the {@link PositionSupplier}'s
 * members may not be the same as the related {@link Portfolio}'s (the supplier may, for example,
 * provide access to a filtered set).
 *
 * @author jeremy
 */
public interface PositionSupplier extends MarketValued {
  /**
   * "Concatenates" the given {@link PositionSupplier}s into a single logical {@link
   * PositionSupplier}.
   *
   * @param suppliers
   *     the {@link PositionSupplier}s to be concatenated
   *
   * @return a {@link PositionSupplier} representing the concatenation of the suppliers
   */
  static PositionSupplier concat(PositionSupplier... suppliers) {
    return new CompoundPositionSupplier(suppliers);
  }

  /**
   * Obtains the key of this {@link PositionSupplier}'s related {@link Portfolio}, if any.
   *
   * @return the related {@link Portfolio}, or {@code null} if none is associated
   */
  PortfolioKey getPortfolioKey();

  /**
   * Obtains this {@link PositionSupplier}'s {@link Position}s as a (new) {@link Stream}.
   *
   * @return a {@link Stream} of {@link Position}s
   */
  Stream<? extends Position> getPositions();

  /**
   * Given an {@link EvaluationContext}, obtains this {@link PositionSupplier}'s {@link Position}s
   * as a (new) {@link Stream} of {@link PositionEvaluationContext}s.
   *
   * @param evaluationContext
   *     the {@link EvaluationContext} in which to perform the evaluation
   *
   * @return a {@link Stream} of {@link PositionEvaluationContext}s
   */
  default Stream<PositionEvaluationContext> getPositionsWithContext(
      EvaluationContext evaluationContext) {
    return getPositions().map(p -> new PositionEvaluationContext(p, evaluationContext));
  }

  /**
   * Obtains the number of {@link Position}s in this {@link PositionSupplier}.
   *
   * @return the number of {@link Position}s
   */
  int size();
}
