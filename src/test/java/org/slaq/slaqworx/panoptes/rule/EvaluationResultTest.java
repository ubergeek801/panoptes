package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.rule.EvaluationResult.Impact;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult.Threshold;

/**
 * {@code EvaluationResultTest} tests the functionality of {@code EvaluationResult}.
 *
 * @author jeremy
 */
public class EvaluationResultTest {
    /**
     * Tests that {@code compare()} behaves as expected for simple Boolean results.
     */
    @Test
    public void testCompareBoolean() {
        EvaluationResult original = new EvaluationResult(true);
        EvaluationResult proposed = new EvaluationResult(false);
        Impact impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "true->false should be a negative impact");

        original = new EvaluationResult(true);
        proposed = new EvaluationResult(true);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "true->true should be a neutral impact");

        original = new EvaluationResult(false);
        proposed = new EvaluationResult(false);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "false->false should be a neutral impact");

        original = new EvaluationResult(false);
        proposed = new EvaluationResult(true);
        impact = proposed.compare(original);
        assertEquals(Impact.POSITIVE, impact, "false->true should be a positive impact");

        original = null;
        proposed = new EvaluationResult(true);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "null->true should be a neutral impact");

        proposed = new EvaluationResult(false);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "null->false should be a negative impact");
    }

    /**
     * Tests that {@code compare()} behaves as expected for value results.
     */
    @Test
    public void testCompareValue() {
        EvaluationResult original = new EvaluationResult(Threshold.ABOVE, 10);
        EvaluationResult proposed = new EvaluationResult(Threshold.ABOVE, 11);
        Impact impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "ABOVE->more ABOVE should be a negative impact");

        original = new EvaluationResult(Threshold.ABOVE, 10);
        proposed = new EvaluationResult(Threshold.ABOVE, 9);
        impact = proposed.compare(original);
        assertEquals(Impact.POSITIVE, impact, "ABOVE->less ABOVE should be a positive impact");

        original = new EvaluationResult(Threshold.ABOVE, 10);
        proposed = new EvaluationResult(Threshold.ABOVE, 10);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "ABOVE->equal ABOVE should be a neutral impact");

        original = new EvaluationResult(Threshold.ABOVE, 10);
        proposed = new EvaluationResult(Threshold.WITHIN, 9);
        impact = proposed.compare(original);
        assertEquals(Impact.POSITIVE, impact, "ABOVE->WITHIN should be a positive impact");

        original = new EvaluationResult(Threshold.BELOW, 10);
        proposed = new EvaluationResult(Threshold.BELOW, 9);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "BELOW->more BELOW should be a negative impact");

        original = new EvaluationResult(Threshold.BELOW, 10);
        proposed = new EvaluationResult(Threshold.BELOW, 11);
        impact = proposed.compare(original);
        assertEquals(Impact.POSITIVE, impact, "BELOW->less BELOW should be a positive impact");

        original = new EvaluationResult(Threshold.BELOW, 10);
        proposed = new EvaluationResult(Threshold.BELOW, 10);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "BELOW->equal BELOW should be a neutral impact");

        original = new EvaluationResult(Threshold.BELOW, 10);
        proposed = new EvaluationResult(Threshold.WITHIN, 11);
        impact = proposed.compare(original);
        assertEquals(Impact.POSITIVE, impact, "BELOW->WITHIN should be a positive impact");

        original = new EvaluationResult(Threshold.WITHIN, 10);
        proposed = new EvaluationResult(Threshold.BELOW, 9);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "WITHIN->BELOW should be a negative impact");

        original = new EvaluationResult(Threshold.WITHIN, 10);
        proposed = new EvaluationResult(Threshold.ABOVE, 11);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "WITHIN->ABOVE should be a negative impact");

        original = new EvaluationResult(Threshold.WITHIN, 10);
        proposed = new EvaluationResult(Threshold.WITHIN, 10);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "WITHIN->WITHIN should be a neutral impact");

        original = null;
        proposed = new EvaluationResult(Threshold.ABOVE, 11);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "null->ABOVE should be a negative impact");

        proposed = new EvaluationResult(Threshold.WITHIN, 10);
        impact = proposed.compare(original);
        assertEquals(Impact.NEUTRAL, impact, "null->WITHIN should be a neutral impact");

        // oddball cases but behavior still specified

        original = new EvaluationResult(Threshold.BELOW, 9);
        proposed = new EvaluationResult(Threshold.ABOVE, 11);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "BELOW->ABOVE should be a negative impact");

        original = new EvaluationResult(Threshold.ABOVE, 11);
        proposed = new EvaluationResult(Threshold.BELOW, 9);
        impact = proposed.compare(original);
        assertEquals(Impact.NEGATIVE, impact, "ABOVE->BELOW should be a negative impact");
    }
}
