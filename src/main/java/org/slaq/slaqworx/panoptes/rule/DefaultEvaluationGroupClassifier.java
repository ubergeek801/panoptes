package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;

/**
 * DefaultEvaluationGroupClassifier is the default EvaluationGroupClassifier; it merely classifies
 * all Positions into the default group.
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
    public EvaluationGroup<?> classify(SecurityProvider securityProvider, Position position) {
        return EvaluationGroup.defaultGroup();
    }
}
