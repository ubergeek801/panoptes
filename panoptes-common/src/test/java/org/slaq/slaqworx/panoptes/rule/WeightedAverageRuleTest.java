package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * {@code WeightedAverageRuleTest} tests the functionality of the {@code WeightedAverageRule}.
 *
 * @author jeremy
 */
public class WeightedAverageRuleTest {
    /**
     * Tests that {@code evaluate()} behaves as expected.
     */
    @Test
    public void testEvaluate() {
        // Referring to TestUtil.p1, the weighted average of moovyRating should be be (132_500 /
        // 1500) = 88.333. So a rule requiring a minimum 89 should fail, while a rule requiring 88
        // should pass.

        WeightedAverageRule<?> rule = new WeightedAverageRule<>(null, "test", null,
                TestUtil.moovyRating, 89d, null, null);
        ValueResult result =
                rule.evaluate(TestUtil.p1, null, TestUtil.defaultTestEvaluationContext());
        assertFalse(result.isPassed(), "rule with 89 lower limit should have failed");

        rule = new WeightedAverageRule<>(null, "test", null, TestUtil.moovyRating, 88d, null, null);
        assertTrue(rule.evaluate(TestUtil.p1, null, TestUtil.defaultTestEvaluationContext())
                .isPassed(), "rule with 88 lower limit should have passed");

        // TestUtil.p1 should have an average duration of 4.0

        rule = new WeightedAverageRule<>(null, "test", null, SecurityAttribute.duration, null, 3.9,
                null);
        assertFalse(rule.evaluate(TestUtil.p1, null, TestUtil.defaultTestEvaluationContext())
                .isPassed(), "rule with 3.9 upper limit should have failed");

        rule = new WeightedAverageRule<>(null, "test", null, SecurityAttribute.duration, null, 4d,
                null);
        assertTrue(rule.evaluate(TestUtil.p1, null, TestUtil.defaultTestEvaluationContext())
                .isPassed(), "rule with 4.0 upper limit should have passed");
    }

    /**
     * Tests that {@code getParameterDescription()} behaves as expected.
     */
    @Test
    public void testGetParameterDescription() {
        WeightedAverageRule<?> rule = new WeightedAverageRule<>(null, "test", null,
                TestUtil.moovyRating, 1.02, null, null);

        // we don't care too much about the description as long as it isn't null
        assertNotNull(rule.getParameterDescription(), "should have obtained parameter description");
    }
}
