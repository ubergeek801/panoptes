package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Supplier;

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
     * Classifies the specified {@code Position} into an {@code EvaluationGroup}.
     *
     * @param positionContext
     *            a {@code PositionEvaluationContext} specifying the {@code Position} to be
     *            classified
     * @return the {@code EvaluationGroup} to be applied to the {@code Position}
     */
    public default EvaluationGroup classify(PositionEvaluationContext positionContext) {
        return classify(() -> positionContext);
    }

    /**
     * Classifies the specified {@code Position} into an {@code EvaluationGroup}.
     *
     * @param positionContextSupplier
     *            a {@code Supplier} providing a {@code PositionEvaluationContext} specifying the
     *            {@code Position} to be classified
     * @return the {@code EvaluationGroup} to be applied to the {@code Position}
     */
    public EvaluationGroup classify(Supplier<PositionEvaluationContext> positionContextSupplier);
}
