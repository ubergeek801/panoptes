package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * {@code DefaultEvaluationGroupClassifier} is the default {@code EvaluationGroupClassifier}; it
 * merely classifies all {@code Position}s into the default group.
 *
 * @author jeremy
 */
public class DefaultEvaluationGroupClassifier implements EvaluationGroupClassifier {
    /**
     * Creates a new DefaultEvaluationGroupClassifier.
     */
    public DefaultEvaluationGroupClassifier() {
        // nothing to do
    }

    @Override
    public EvaluationGroup<?> classify(Position position) {
        return EvaluationGroup.defaultGroup();
    }
}
