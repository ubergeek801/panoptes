package org.slaq.slaqworx.panoptes.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.NoDataException;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * Tests the functionality of the {@link WeightedAveragePositionCalculator}.
 *
 * @author jeremy
 */
public class WeightedAveragePositionCalculatorTest {
  /** Tests that {@code calculateWeightedAverage()} behaves as expected. */
  @Test
  public void testCalculateWeightedAverage() {
    WeightedAveragePositionCalculator<Double> calculator =
        new WeightedAveragePositionCalculator<>(TestUtil.moovyRating);

    // 1000 of s1 = 1000 * s1.moovyRating = 1000 * 90 = 90_000
    // 500 of s2 = 500 * s2.moovyRating = 500 * 85 = 42_500
    // weighted average = (90_000 + 42_500) / (1000 + 500) = 132_500 / 1500 = 88.333...

    assertEquals(
        (132_500d / 1500),
        calculator.calculate(
            TestUtil.p1.getPositionsWithContext(TestUtil.defaultTestEvaluationContext())),
        TestUtil.EPSILON,
        "unexpected weighted average for p1");

    // 500 of s1 = 500 * s1.moovyRating = 500 * 90 = 45_000
    // 1000 of s2 = 1000 * s2.moovyRating = 1000 * 85 = 85_000
    // weighted average = (45_000 + 85_000) / (1000 + 500) = 130_000 / 1500 = 86.666...

    assertEquals(
        (130_000d / 1500),
        calculator.calculate(
            TestUtil.p2.getPositionsWithContext(TestUtil.defaultTestEvaluationContext())),
        TestUtil.EPSILON,
        "unexpected weighted average for p2");

    calculator = new WeightedAveragePositionCalculator<>(TestUtil.fetchRating);

    // s2 has no fetchRating -> NoDataException

    try {
      calculator.calculate(
          TestUtil.p1.getPositionsWithContext(TestUtil.defaultTestEvaluationContext()));
      fail("calculation on Position with unavailable data should cause NoDataException");
    } catch (NoDataException e) {
      // expected
    }

    // same expectation as above

    try {
      calculator.calculate(
          TestUtil.p2.getPositionsWithContext(TestUtil.defaultTestEvaluationContext()));
      fail("calculation on Position with unavailable data should cause NoDataException");
    } catch (NoDataException e) {
      // expected
    }
  }
}
