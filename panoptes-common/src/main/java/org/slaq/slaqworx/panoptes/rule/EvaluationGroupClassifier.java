package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * Classifies {@link Position}s into {@link EvaluationGroup}s for the purpose of grouping rule
 * evaluation results.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface EvaluationGroupClassifier {
  /**
   * Obtains the default ({@link Portfolio}-level) classifier.
   *
   * @return the default classifier
   */
  @Nonnull
  static EvaluationGroupClassifier defaultClassifier() {
    return new DefaultEvaluationGroupClassifier();
  }

  /**
   * Classifies the specified {@link Position} into an {@link EvaluationGroup}.
   *
   * @param positionContext a {@link PositionEvaluationContext} specifying the {@link Position} to
   *     be classified
   * @return the {@link EvaluationGroup} to be applied to the {@link Position}
   */
  @Nonnull
  default EvaluationGroup classify(@Nonnull PositionEvaluationContext positionContext) {
    return classify(() -> positionContext);
  }

  /**
   * Classifies the specified {@link Position} into an {@link EvaluationGroup}.
   *
   * @param positionContextSupplier a {@link Supplier} providing a {@link PositionEvaluationContext}
   *     specifying the {@link Position} to be classified
   * @return the {@link EvaluationGroup} to be applied to the {@link Position}
   */
  @Nonnull
  EvaluationGroup classify(@Nonnull Supplier<PositionEvaluationContext> positionContextSupplier);
}
