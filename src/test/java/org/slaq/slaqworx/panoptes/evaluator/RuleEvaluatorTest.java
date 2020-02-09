package org.slaq.slaqworx.panoptes.evaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.GenericRule;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleResult;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;

/**
 * {@code RuleEvaluatorTest} tests the functionality of the {@code RuleEvaluator}.
 *
 * @author jeremy
 */
public class RuleEvaluatorTest {
    /**
     * Tests that {@code Position} classification behaves as expected.
     */
    @Test
    public void testClassify() {
        // a dumb classifier that merely "classifies" by security ID
        EvaluationGroupClassifier classifier =
                (ctx -> new EvaluationGroup(ctx.getPosition().getSecurityKey().getId(), "id"));
        Rule rule = new GenericRule(null, "dummy rule", classifier) {
            @Override
            protected RuleResult eval(PositionSupplier portfolioPositions,
                    PositionSupplier benchmarkPositions, EvaluationGroup evaluationGroup,
                    EvaluationContext evaluationContext) {
                // will not actually be invoked anyway
                return null;
            }
        };

        Map<EvaluationGroup, PositionSupplier> classifiedPositions =
                new RuleEvaluator(rule, null, null, TestUtil.defaultTestEvaluationContext())
                        .classify(TestUtil.p1, null);
        assertEquals(2, classifiedPositions.size(),
                "Positions in distinct Securities should have been classified distinctly");
    }

    /**
     * Tests that {@code Position} evaluation behaves as expected.
     */
    @Test
    public void testEvaluate() {
        // a dumb classifier that merely "classifies" by security ID
        EvaluationGroupClassifier classifier =
                (ctx -> new EvaluationGroup(ctx.getPosition().getSecurityKey().getId(), "id"));
        // a dumb filter that matches Positions in s1
        Predicate<PositionEvaluationContext> filter =
                (c -> c.getPosition().getSecurityKey().equals(TestUtil.s1.getKey()));
        Rule rule = new WeightedAverageRule<>(null, "test rule", filter, SecurityAttribute.duration,
                0d, 3.9, classifier);

        EvaluationResult result =
                new RuleEvaluator(rule, TestUtil.p1, null, TestUtil.defaultTestEvaluationContext())
                        .call();
        assertNotNull(result, "result should never be null");
        Map<EvaluationGroup, RuleResult> groupedResults = result.getResults();
        assertNotNull(groupedResults, "result groups should never be null");
        assertEquals(1, groupedResults.size(),
                "a single (filtered) Position should result in a single group");
        RuleResult ruleResult = groupedResults.values().iterator().next();
        assertFalse(ruleResult.isPassed(), "rule with 3.9 upper limit should have failed");

        rule = new WeightedAverageRule<>(null, "test rule", filter, SecurityAttribute.duration, 3.9,
                4.1, classifier);
        result = new RuleEvaluator(rule, TestUtil.p1, null, TestUtil.defaultTestEvaluationContext())
                .call();
        assertTrue(result.isPassed(),
                "rule with 3.9 lower limit and 4.1 upper limit should have passed");

        // p1's concentration in s1 is 66.667% so should fail this rule
        rule = new ConcentrationRule(null, "test rule", filter, null, 0.65, null);
        result = new RuleEvaluator(rule, TestUtil.p1, null, TestUtil.defaultTestEvaluationContext())
                .call();
        assertFalse(result.isPassed(), "rule with 65% upper limit should have failed");
        // ...and should pass this rule
        rule = new ConcentrationRule(null, "test rule", filter, null, 0.67, null);
        result = new RuleEvaluator(rule, TestUtil.p1, null, TestUtil.defaultTestEvaluationContext())
                .call();
        assertTrue(result.isPassed(), "rule with 67% upper limit should have passed");
    }

    /**
     * Tests that {@code Position} evaluation behaves as expected with a benchmark-relative
     * {@code Rule}.
     */
    @Test
    public void testEvaluate_benchmarkRelative() {
        // include only Positions that are not in the TestUtil.s2 Security
        Predicate<PositionEvaluationContext> filter =
                (c -> !c.getPosition().getSecurityKey().equals(TestUtil.s2.getKey()));

        // Once filtered, p1's sole Position will be 1000 of s1, which has weighted moovyRating
        // (1000 * 90) / 1000 = 90. Meanwhile, p3's Positions will be 500 of s1 (500 * 90) and 200
        // of s3 (200 * 80), for a weighted moovyRating of (45_000 + 16_000) / 700 = 87.142857143.
        // Thus p1's weighted average is 1.032786885 * p3's.

        Rule rule = new WeightedAverageRule<>(null, "test", filter, TestUtil.moovyRating, 1.035,
                null, null);
        EvaluationContext context = TestUtil.defaultTestEvaluationContext();
        EvaluationResult result = new RuleEvaluator(rule, TestUtil.p1, TestUtil.p3, context).call();
        assertFalse(result.isPassed(), "rule with 103.5% lower limit should have failed");

        rule = new WeightedAverageRule<>(null, "test", filter, TestUtil.moovyRating, 1.03, null,
                null);
        context = TestUtil.defaultTestEvaluationContext();
        result = new RuleEvaluator(rule, TestUtil.p1, TestUtil.p3, context).call();
        assertTrue(result.isPassed(), "rule with 103% lower limit should have passed");

        // p1's concentration in non-s2 is 1000 / 1500 = 0.666...; p3's concentration is 700 / 1700
        // = 0.411764706; p1 is thus 1.619047619 of the benchmark and should thus fail this rule
        rule = new ConcentrationRule(null, "test rule", filter, 1.62, null, null);
        context = TestUtil.defaultTestEvaluationContext();
        result = new RuleEvaluator(rule, TestUtil.p1, TestUtil.p3, context).call();
        assertFalse(result.isPassed(), "rule with 162% lower limit should have failed");
        // ...and should pass this rule
        rule = new ConcentrationRule(null, "test rule", filter, 1.619, null, null);
        context = TestUtil.defaultTestEvaluationContext();
        result = new RuleEvaluator(rule, TestUtil.p1, TestUtil.p3, context).call();
        assertTrue(result.isPassed(), "rule with 161.9% lower limit should have passed");
    }
}
