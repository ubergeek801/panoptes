package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * {@code EvaluationGroupClassifier} classifies {@code Position}s into {@code EvaluationGroup}s for
 * the purpose of grouping rule evaluation results.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface EvaluationGroupClassifier {
    /**
     * Obtains the default ({@code Portfolio}-level) classifier.
     *
     * @return the default classifier
     */
    public static EvaluationGroupClassifier defaultClassifier() {
        return new DefaultEvaluationGroupClassifier();
    }

    /**
     * Classifies the given {@code Position} into an {@code EvaluationGroup}.
     *
     * @param position
     *            the {@code Position} to be classified
     * @return the {@code EvaluationGroup} to be applied to the {@code Position}
     */
    public EvaluationGroup classify(Position position);
}
