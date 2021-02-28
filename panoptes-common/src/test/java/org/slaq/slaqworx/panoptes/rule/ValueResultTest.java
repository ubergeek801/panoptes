package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Impact;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;

/**
 * Tests the functionality of the {@link ValueResult}.
 *
 * @author jeremy
 */
public class ValueResultTest {
  /**
   * Tests that {@code compare()} behaves as expected for simple Boolean results.
   */
  @Test
  public void testCompareBoolean() {
    ValueResult original = new ValueResult(true);
    ValueResult proposed = new ValueResult(false);
    Impact impact = proposed.compare(original);
    assertEquals(Impact.NEGATIVE, impact, "true->false should be a negative impact");

    original = new ValueResult(true);
    proposed = new ValueResult(true);
    impact = proposed.compare(original);
    assertEquals(Impact.NEUTRAL, impact, "true->true should be a neutral impact");

    original = new ValueResult(false);
    proposed = new ValueResult(false);
    impact = proposed.compare(original);
    assertEquals(Impact.NEUTRAL, impact, "false->false should be a neutral impact");

    original = new ValueResult(false);
    proposed = new ValueResult(true);
    impact = proposed.compare(original);
    assertEquals(Impact.POSITIVE, impact, "false->true should be a positive impact");

    original = null;
    proposed = new ValueResult(true);
    impact = proposed.compare(original);
    assertEquals(Impact.NEUTRAL, impact, "null->true should be a neutral impact");

    proposed = new ValueResult(false);
    impact = proposed.compare(original);
    assertEquals(Impact.NEGATIVE, impact, "null->false should be a negative impact");
  }

  /**
   * Tests that {@code compare()} behaves as expected for value results.
   */
  @Test
  public void testCompareValue() {
    ValueResult original = new ValueResult(Threshold.ABOVE, 10);
    ValueResult proposed = new ValueResult(Threshold.ABOVE, 11);
    Impact impact = proposed.compare(original);
    assertEquals(Impact.NEGATIVE, impact, "ABOVE->more ABOVE should be a negative impact");

    original = new ValueResult(Threshold.ABOVE, 10);
    proposed = new ValueResult(Threshold.ABOVE, 9);
    impact = proposed.compare(original);
    assertEquals(Impact.POSITIVE, impact, "ABOVE->less ABOVE should be a positive impact");

    original = new ValueResult(Threshold.ABOVE, 10);
    proposed = new ValueResult(Threshold.ABOVE, 10);
    impact = proposed.compare(original);
    assertEquals(Impact.NEUTRAL, impact, "ABOVE->equal ABOVE should be a neutral impact");

    original = new ValueResult(Threshold.ABOVE, 10);
    proposed = new ValueResult(Threshold.WITHIN, 9);
    impact = proposed.compare(original);
    assertEquals(Impact.POSITIVE, impact, "ABOVE->WITHIN should be a positive impact");

    original = new ValueResult(Threshold.BELOW, 10);
    proposed = new ValueResult(Threshold.BELOW, 9);
    impact = proposed.compare(original);
    assertEquals(Impact.NEGATIVE, impact, "BELOW->more BELOW should be a negative impact");

    original = new ValueResult(Threshold.BELOW, 10);
    proposed = new ValueResult(Threshold.BELOW, 11);
    impact = proposed.compare(original);
    assertEquals(Impact.POSITIVE, impact, "BELOW->less BELOW should be a positive impact");

    original = new ValueResult(Threshold.BELOW, 10);
    proposed = new ValueResult(Threshold.BELOW, 10);
    impact = proposed.compare(original);
    assertEquals(Impact.NEUTRAL, impact, "BELOW->equal BELOW should be a neutral impact");

    original = new ValueResult(Threshold.BELOW, 10);
    proposed = new ValueResult(Threshold.WITHIN, 11);
    impact = proposed.compare(original);
    assertEquals(Impact.POSITIVE, impact, "BELOW->WITHIN should be a positive impact");

    original = new ValueResult(Threshold.WITHIN, 10);
    proposed = new ValueResult(Threshold.BELOW, 9);
    impact = proposed.compare(original);
    assertEquals(Impact.NEGATIVE, impact, "WITHIN->BELOW should be a negative impact");

    original = new ValueResult(Threshold.WITHIN, 10);
    proposed = new ValueResult(Threshold.ABOVE, 11);
    impact = proposed.compare(original);
    assertEquals(Impact.NEGATIVE, impact, "WITHIN->ABOVE should be a negative impact");

    original = new ValueResult(Threshold.WITHIN, 10);
    proposed = new ValueResult(Threshold.WITHIN, 10);
    impact = proposed.compare(original);
    assertEquals(Impact.NEUTRAL, impact, "WITHIN->WITHIN should be a neutral impact");

    original = null;
    proposed = new ValueResult(Threshold.ABOVE, 11);
    impact = proposed.compare(original);
    assertEquals(Impact.NEGATIVE, impact, "null->ABOVE should be a negative impact");

    proposed = new ValueResult(Threshold.WITHIN, 10);
    impact = proposed.compare(original);
    assertEquals(Impact.NEUTRAL, impact, "null->WITHIN should be a neutral impact");

    // oddball cases but behavior still specified

    original = new ValueResult(Threshold.BELOW, 9);
    proposed = new ValueResult(Threshold.ABOVE, 11);
    impact = proposed.compare(original);
    assertEquals(Impact.NEGATIVE, impact, "BELOW->ABOVE should be a negative impact");

    original = new ValueResult(Threshold.ABOVE, 11);
    proposed = new ValueResult(Threshold.BELOW, 9);
    impact = proposed.compare(original);
    assertEquals(Impact.NEGATIVE, impact, "ABOVE->BELOW should be a negative impact");
  }
}
