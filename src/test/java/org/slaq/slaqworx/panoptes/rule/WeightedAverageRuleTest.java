package org.slaq.slaqworx.panoptes.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * WeightedAverageRuleTest tests the functionality of the WeightedAverageRule.
 *
 * @author jeremy
 */
public class WeightedAverageRuleTest {
    /**
     * Tests that evaluate() behaves as expected.
     */
    @Test
    public void testEvaluate() {
        // Referring to TestUtil.p1 and TestUtil.p2, the ratio of p1.weightedAverage /
        // p2.weightedAverage should be (132_500 / 1500) / (130_000 / 1500) = 132_500 / 130_000 =
        // 1.019230769. So a rule requiring 102% of the benchmark (p2) should fail, while a rule
        // requiring 101.9% should pass.

        ValueRule rule = new WeightedAverageRule(null, "test", null, TestUtil.moovyRating, 1.02,
                null, null, null);
        assertFalse("rule with 102% lower limit should have failed",
                rule.evaluate(TestUtil.p1, TestUtil.p2));

        rule = new WeightedAverageRule(null, "test", null, TestUtil.moovyRating, 1.015, null, null,
                null);
        assertTrue("rule with 101.5% lower limit should have passed",
                rule.evaluate(TestUtil.p1, TestUtil.p2));

        // both TestUtil.p1 and TestUtil.p2 should have an average duration of 4

        rule = new WeightedAverageRule(null, "test", null, SecurityAttribute.duration, null, 3.9,
                null, null);
        assertFalse("rule with 3.9 upper limit should have failed",
                rule.evaluate(TestUtil.p1, null));

        rule = new WeightedAverageRule(null, "test", null, SecurityAttribute.duration, null, 4d,
                null, null);
        assertTrue("rule with 4 upper limit should have passed", rule.evaluate(TestUtil.p1, null));
    }
}
