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
        // Referring to TestUtil.p1 and TestUtil.p2, the ratio of p1.weightedAverage /
        // p2.weightedAverage should be (132_500 / 1500) / (130_000 / 1500) = 132_500 / 130_000 =
        // 1.019230769. So a rule requiring 102% of the benchmark (p2) should fail, while a rule
        // requiring 101.5% should pass.

        WeightedAverageRule<?> rule = new WeightedAverageRule<>(null, "test", null,
                TestUtil.moovyRating, 1.02, null, null);
        RuleResult result = rule.evaluate(TestUtil.p1, TestUtil.p2, null,
                TestUtil.defaultTestEvaluationContext());
        assertFalse(result.isPassed(), "rule with 102% lower limit should have failed");

        rule = new WeightedAverageRule<>(null, "test", null, TestUtil.moovyRating, 1.015, null,
                null);
        assertTrue(
                rule.evaluate(TestUtil.p1, TestUtil.p2, null,
                        TestUtil.defaultTestEvaluationContext()).isPassed(),
                "rule with 101.5% lower limit should have passed");

        // both TestUtil.p1 and TestUtil.p2 should have an average duration of 4.0

        rule = new WeightedAverageRule<>(null, "test", null, SecurityAttribute.duration, null, 3.9,
                null);
        assertFalse(rule.evaluate(TestUtil.p1, null, null, TestUtil.defaultTestEvaluationContext())
                .isPassed(), "rule with 3.9 upper limit should have failed");

        rule = new WeightedAverageRule<>(null, "test", null, SecurityAttribute.duration, null, 4d,
                null);
        assertTrue(rule.evaluate(TestUtil.p1, null, null, TestUtil.defaultTestEvaluationContext())
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
