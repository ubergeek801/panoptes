package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * The default {@link EvaluationGroupClassifier}; it merely classifies all {@link Position}s into
 * the default group.
 *
 * @author jeremy
 */
public class DefaultEvaluationGroupClassifier implements EvaluationGroupClassifier {
  /**
   * Creates a new {@link DefaultEvaluationGroupClassifier}.
   */
  public DefaultEvaluationGroupClassifier() {
    // nothing to do
  }

  @Nonnull
  @Override
  public EvaluationGroup classify(@Nonnull Supplier<PositionEvaluationContext> positionContextSupplier) {
    return EvaluationGroup.defaultGroup();
  }
}
