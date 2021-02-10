package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Supplier;

/**
 * The default {@code EvaluationGroupClassifier}; it merely classifies all {@code Position}s into
 * the default group.
 *
 * @author jeremy
 */
public class DefaultEvaluationGroupClassifier implements EvaluationGroupClassifier {
    /**
     * Creates a new {@code DefaultEvaluationGroupClassifier}.
     */
    public DefaultEvaluationGroupClassifier() {
        // nothing to do
    }

    @Override
    public EvaluationGroup classify(Supplier<PositionEvaluationContext> positionContextSupplier) {
        return EvaluationGroup.defaultGroup();
    }
}
