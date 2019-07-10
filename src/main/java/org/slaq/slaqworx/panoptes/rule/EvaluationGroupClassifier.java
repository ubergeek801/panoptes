package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * EvaluationGroupClassifier classifies Positions into EvaluationGroups for the purpose of grouping
 * rule evaluation results.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface EvaluationGroupClassifier {
    /**
     * Obtains the default (Portfolio-level) classifier.
     *
     * @return the default classifier
     */
    public static EvaluationGroupClassifier defaultClassifier() {
        return p -> EvaluationGroup.defaultGroup();
    }

    /**
     * Classifies the given Position into an EvaluationGroup.
     *
     * @param position
     *            the Position to be classified
     * @return the EvaluationGroup to be applied to the Position
     */
    public EvaluationGroup classify(Position position);
}
