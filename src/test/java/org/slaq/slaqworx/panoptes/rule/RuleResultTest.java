package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.rule.RuleResult.Impact;
import org.slaq.slaqworx.panoptes.rule.RuleResult.Threshold;

/**
 * {@code RuleResultTest} tests the functionality of {@code RuleResult}.
 *
 * @author jeremy
 */
public class RuleResultTest {
    /**
     * Tests that {@code compare()} behaves as expected for simple Boolean results.
     */
    @Test
    public void testCompareBoolean() {
        RuleResult original = new RuleResult(true);
        RuleResult proposed = new RuleResult(false);
        Impact impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "true->false should be a negative impact");

        original = new RuleResult(true);
        proposed = new RuleResult(true);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "true->true should be a neutral impact");

        original = new RuleResult(false);
        proposed = new RuleResult(false);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "false->false should be a neutral impact");

        original = new RuleResult(false);
        proposed = new RuleResult(true);
        impact = proposed.compare(original);
        assertEquals(Impact.POSITIVE, impact, "false->true should be a positive impact");

        original = null;
        proposed = new RuleResult(true);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "null->true should be a neutral impact");

        proposed = new RuleResult(false);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "null->false should be a negative impact");
    }

    /**
     * Tests that {@code compare()} behaves as expected for value results.
     */
    @Test
    public void testCompareValue() {
        RuleResult original = new RuleResult(Threshold.ABOVE, 10);
        RuleResult proposed = new RuleResult(Threshold.ABOVE, 11);
        Impact impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "ABOVE->more ABOVE should be a negative impact");

        original = new RuleResult(Threshold.ABOVE, 10);
        proposed = new RuleResult(Threshold.ABOVE, 9);
        impact = proposed.compare(original);
        assertEquals(Impact.POSITIVE, impact, "ABOVE->less ABOVE should be a positive impact");

        original = new RuleResult(Threshold.ABOVE, 10);
        proposed = new RuleResult(Threshold.ABOVE, 10);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "ABOVE->equal ABOVE should be a neutral impact");

        original = new RuleResult(Threshold.ABOVE, 10);
        proposed = new RuleResult(Threshold.WITHIN, 9);
        impact = proposed.compare(original);
        assertEquals(Impact.POSITIVE, impact, "ABOVE->WITHIN should be a positive impact");

        original = new RuleResult(Threshold.BELOW, 10);
        proposed = new RuleResult(Threshold.BELOW, 9);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "BELOW->more BELOW should be a negative impact");

        original = new RuleResult(Threshold.BELOW, 10);
        proposed = new RuleResult(Threshold.BELOW, 11);
        impact = proposed.compare(original);
        assertEquals(Impact.POSITIVE, impact, "BELOW->less BELOW should be a positive impact");

        original = new RuleResult(Threshold.BELOW, 10);
        proposed = new RuleResult(Threshold.BELOW, 10);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "BELOW->equal BELOW should be a neutral impact");

        original = new RuleResult(Threshold.BELOW, 10);
        proposed = new RuleResult(Threshold.WITHIN, 11);
        impact = proposed.compare(original);
        assertEquals(Impact.POSITIVE, impact, "BELOW->WITHIN should be a positive impact");

        original = new RuleResult(Threshold.WITHIN, 10);
        proposed = new RuleResult(Threshold.BELOW, 9);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "WITHIN->BELOW should be a negative impact");

        original = new RuleResult(Threshold.WITHIN, 10);
        proposed = new RuleResult(Threshold.ABOVE, 11);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "WITHIN->ABOVE should be a negative impact");

        original = new RuleResult(Threshold.WITHIN, 10);
        proposed = new RuleResult(Threshold.WITHIN, 10);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "WITHIN->WITHIN should be a neutral impact");

        original = null;
        proposed = new RuleResult(Threshold.ABOVE, 11);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "null->ABOVE should be a negative impact");

        proposed = new RuleResult(Threshold.WITHIN, 10);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "null->WITHIN should be a neutral impact");

        // oddball cases but behavior still specified

        original = new RuleResult(Threshold.BELOW, 9);
        proposed = new RuleResult(Threshold.ABOVE, 11);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "BELOW->ABOVE should be a negative impact");

        original = new RuleResult(Threshold.ABOVE, 11);
        proposed = new RuleResult(Threshold.BELOW, 9);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "ABOVE->BELOW should be a negative impact");
    }
}
