package org.slaq.slaqworx.panoptes.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
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
        TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

        // Referring to TestUtil.p1 and TestUtil.p2, the ratio of p1.weightedAverage /
        // p2.weightedAverage should be (132_500 / 1500) / (130_000 / 1500) = 132_500 / 130_000 =
        // 1.019230769. So a rule requiring 102% of the benchmark (p2) should fail, while a rule
        // requiring 101.5% should pass.

        ValueRule rule =
                new WeightedAverageRule(null, "test", null, TestUtil.moovyRating, 1.02, null, null);
        assertFalse("rule with 102% lower limit should have failed",
                rule.evaluate(TestUtil.p1, TestUtil.p2, new EvaluationContext(securityProvider))
                        .isPassed());

        rule = new WeightedAverageRule(null, "test", null, TestUtil.moovyRating, 1.015, null, null);
        assertTrue("rule with 101.5% lower limit should have passed",
                rule.evaluate(TestUtil.p1, TestUtil.p2, new EvaluationContext(securityProvider))
                        .isPassed());

        // both TestUtil.p1 and TestUtil.p2 should have an average duration of 4.0

        rule = new WeightedAverageRule(null, "test", null, SecurityAttribute.duration, null, 3.9,
                null);
        assertFalse("rule with 3.9 upper limit should have failed", rule
                .evaluate(TestUtil.p1, null, new EvaluationContext(securityProvider)).isPassed());

        rule = new WeightedAverageRule(null, "test", null, SecurityAttribute.duration, null, 4d,
                null);
        assertTrue("rule with 4.0 upper limit should have passed", rule
                .evaluate(TestUtil.p1, null, new EvaluationContext(securityProvider)).isPassed());
    }
}
