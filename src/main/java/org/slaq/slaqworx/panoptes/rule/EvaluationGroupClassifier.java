package org.slaq.slaqworx.panoptes.rule;

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
     * @param positionContext
     *            a {@code PositionEvaluationContext} specifying the {@code Position} to be
     *            classified
     * @return the {@code EvaluationGroup} to be applied to the {@code Position}
     */
    public EvaluationGroup classify(PositionEvaluationContext positionContext);
}
