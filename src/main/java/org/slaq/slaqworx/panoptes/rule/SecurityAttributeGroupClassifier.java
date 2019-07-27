package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * SecurityAttributeGroupClassifier is an EvaluationGroupClassifier which classifies Positions based
 * on the value of a specified SecurityAttribute.
 *
 * @author jeremy
 */
public class SecurityAttributeGroupClassifier implements EvaluationGroupClassifier {
    private static final long serialVersionUID = 1L;

    private final SecurityAttribute<?> securityAttribute;

    /**
     * Creates a new SecurityAttributeGroupClassifier which classifies Positions based on the
     * specified SecurityAttribute.
     *
     * @param securityAttribute
     *            the SecurityAttribute on which to classify Positions
     */
    public SecurityAttributeGroupClassifier(SecurityAttribute<?> securityAttribute) {
        this.securityAttribute = securityAttribute;
    }

    @Override
    public EvaluationGroup<SecurityAttribute<?>> classify(Position position) {
        return new EvaluationGroup<>(
                String.valueOf(position.getSecurity().getAttributeValue(securityAttribute)),
                securityAttribute);
    }

    /**
     * Obtains the SecurityAttribute on which this classifier aggregates.
     *
     * @return the SecurityAttribute
     */
    public SecurityAttribute<?> getSecurityAttribute() {
        return securityAttribute;
    }
}
