package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;

/**
 * {@code SecurityAttributeGroupClassifierTest} tests the functionality of the
 * {@code SecurityAttributeGroupClassifier}.
 *
 * @author jeremy
 */
public class SecurityAttributeGroupClassifierTest {
    /**
     * Tests that {@code classify()} behaves as expected.
     */
    @Test
    public void testClassify() {
        SecurityAttributeGroupClassifier classifier =
                new SecurityAttributeGroupClassifier(SecurityAttribute.country);

        SimplePosition position = new SimplePosition(100d, TestUtil.s1.getKey());
        PositionEvaluationContext positionContext =
                new PositionEvaluationContext(position, TestUtil.defaultTestEvaluationContext());
        EvaluationGroup group = classifier.classify(positionContext);

        assertEquals(SecurityAttribute.country.getName(), group.getAggregationKey(),
                "aggregation key should match SecurityAttribute name");
        assertEquals(TestUtil.s1.getAttributeValue(SecurityAttribute.country), group.getId(),
                "group ID should match SecurityAttribute.country value");
    }

    /**
     * Tests that JSON configuration of a {@code SecurityAttributeGroupClassifier} behaves as
     * expected.
     */
    @Test
    public void testConfiguration() {
        SecurityAttributeGroupClassifier classifier =
                new SecurityAttributeGroupClassifier(SecurityAttribute.issuer);
        String configuration = classifier.getJsonConfiguration();
        SecurityAttributeGroupClassifier configured =
                SecurityAttributeGroupClassifier.fromJson(configuration);

        assertEquals(classifier.getSecurityAttribute(), configured.getSecurityAttribute(),
                "configured classiifer should have same attribute as original");
    }
}
