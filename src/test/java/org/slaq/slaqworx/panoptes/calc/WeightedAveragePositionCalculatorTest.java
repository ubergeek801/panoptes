package org.slaq.slaqworx.panoptes.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * {@code WeightedAveragePositionCalculatorTest} tests the functionality of the
 * {@code WeightedAveragePositionCalculator}.
 *
 * @author jeremy
 */
public class WeightedAveragePositionCalculatorTest {
    /**
     * Tests that {@code calculateWeightedAverage()} behaves as expected.
     */
    @Test
    public void testCalculateWeightedAverage() {
        WeightedAveragePositionCalculator calculator =
                new WeightedAveragePositionCalculator(TestUtil.moovyRating);

        // 1000 of s1 = 1000 * s1.moovyRating = 1000 * 90 = 90_000
        // 500 of s2 = 500 * s2.moovyRating = 500 * 85 = 42_500
        // weighted average = (90_000 + 42_500) / (1000 + 500) = 132_500 / 1500 = 88.333...

        assertEquals((132_500d / 1500),
                calculator.calculate(TestUtil.p1.getPositionsWithContext(new EvaluationContext())),
                TestUtil.EPSILON, "unexpected weighted average for p1");

        // 500 of s1 = 500 * s1.moovyRating = 500 * 90 = 45_000
        // 1000 of s2 = 1000 * s2.moovyRating = 1000 * 85 = 85_000
        // weighted average = (45_000 + 85_000) / (1000 + 500) = 130_000 / 1500 = 86.666...

        assertEquals((130_000d / 1500),
                calculator.calculate(TestUtil.p2.getPositionsWithContext(new EvaluationContext())),
                TestUtil.EPSILON, "unexpected weighted average for p2");

        calculator = new WeightedAveragePositionCalculator(TestUtil.fetchRating);

        // 1000 of s1 = 1000 * s1.fetchRating = 1000 * 88 = 88_000
        // 500 of s2 = 500 * s2.fetchRating = 500 * (no rating) = (not applicable)
        // weighted average = 88_000 / 1000 = 88

        assertEquals(88d,
                calculator.calculate(TestUtil.p1.getPositionsWithContext(new EvaluationContext())),
                TestUtil.EPSILON, "unexpected weighted average for p1");

        // 500 of s1 = 500 * s1.fetchRating = 500 * 88 = 44_000
        // 1000 of s2 = 1000 * s2.fetchRating = 1000 * (no rating) = (not applicable)
        // weighted average = 44_000 / 500 = 88

        assertEquals(88d,
                calculator.calculate(TestUtil.p2.getPositionsWithContext(new EvaluationContext())),
                TestUtil.EPSILON, "unexpected weighted average for p2");
    }
}
