package org.slaq.slaqworx.panoptes.rule;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.rule.EvaluationResult.Impact;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult.Threshold;

/**
 * EvaluationResultTest tests the functionality of EvaluationResult.
 *
 * @author jeremy
 */
public class EvaluationResultTest {
    /**
     * Tests that compare() behaves as expected for simple Boolean results.
     */
    @Test
    public void testCompareBoolean() {
        EvaluationResult original = new EvaluationResult(true);
        EvaluationResult proposed = new EvaluationResult(false);
        Impact impact = proposed.compare(original);
        assertEquals("true->false should be a negative impact", Impact.NEGATIVE, impact);

        original = new EvaluationResult(true);
        proposed = new EvaluationResult(true);
        impact = proposed.compare(original);
        assertEquals("true->true should be a neutral impact", Impact.NEUTRAL, impact);

        original = new EvaluationResult(false);
        proposed = new EvaluationResult(false);
        impact = proposed.compare(original);
        assertEquals("false->false should be a neutral impact", Impact.NEUTRAL, impact);

        original = new EvaluationResult(false);
        proposed = new EvaluationResult(true);
        impact = proposed.compare(original);
        assertEquals("false->true should be a positive impact", Impact.POSITIVE, impact);

        // bonus null test
        original = null;
        proposed = new EvaluationResult(true);
        impact = proposed.compare(original);
        assertEquals("*->null should be a null impact", Impact.UNKNOWN, impact);
    }

    /**
     * Tests that compare() behaves as expected for value results.
     */
    @Test
    public void testCompareValue() {
        EvaluationResult original = new EvaluationResult(Threshold.ABOVE, 10);
        EvaluationResult proposed = new EvaluationResult(Threshold.ABOVE, 11);
        Impact impact = proposed.compare(original);
        assertEquals("ABOVE->more ABOVE should be a negative impact", Impact.NEGATIVE, impact);

        original = new EvaluationResult(Threshold.ABOVE, 10);
        proposed = new EvaluationResult(Threshold.ABOVE, 9);
        impact = proposed.compare(original);
        assertEquals("ABOVE->less ABOVE should be a positive impact", Impact.POSITIVE, impact);

        original = new EvaluationResult(Threshold.ABOVE, 10);
        proposed = new EvaluationResult(Threshold.ABOVE, 10);
        impact = proposed.compare(original);
        assertEquals("ABOVE->equal ABOVE should be a neutral impact", Impact.NEUTRAL, impact);

        original = new EvaluationResult(Threshold.ABOVE, 10);
        proposed = new EvaluationResult(Threshold.WITHIN, 9);
        impact = proposed.compare(original);
        assertEquals("ABOVE->WITHIN should be a positive impact", Impact.POSITIVE, impact);

        original = new EvaluationResult(Threshold.BELOW, 10);
        proposed = new EvaluationResult(Threshold.BELOW, 9);
        impact = proposed.compare(original);
        assertEquals("BELOW->more BELOW should be a negative impact", Impact.NEGATIVE, impact);

        original = new EvaluationResult(Threshold.BELOW, 10);
        proposed = new EvaluationResult(Threshold.BELOW, 11);
        impact = proposed.compare(original);
        assertEquals("BELOW->less BELOW should be a positive impact", Impact.POSITIVE, impact);

        original = new EvaluationResult(Threshold.BELOW, 10);
        proposed = new EvaluationResult(Threshold.BELOW, 10);
        impact = proposed.compare(original);
        assertEquals("BELOW->equal BELOW should be a neutral impact", Impact.NEUTRAL, impact);

        original = new EvaluationResult(Threshold.BELOW, 10);
        proposed = new EvaluationResult(Threshold.WITHIN, 11);
        impact = proposed.compare(original);
        assertEquals("BELOW->WITHIN should be a positive impact", Impact.POSITIVE, impact);

        original = new EvaluationResult(Threshold.WITHIN, 10);
        proposed = new EvaluationResult(Threshold.BELOW, 9);
        impact = proposed.compare(original);
        assertEquals("WITHIN->BELOW should be a negative impact", Impact.NEGATIVE, impact);

        original = new EvaluationResult(Threshold.WITHIN, 10);
        proposed = new EvaluationResult(Threshold.ABOVE, 11);
        impact = proposed.compare(original);
        assertEquals("WITHIN->ABOVE should be a negative impact", Impact.NEGATIVE, impact);

        original = new EvaluationResult(Threshold.WITHIN, 10);
        proposed = new EvaluationResult(Threshold.WITHIN, 10);
        impact = proposed.compare(original);
        assertEquals("WITHIN->WITHIN should be a neutral impact", Impact.NEUTRAL, impact);

        // oddball cases but behavior still specified

        original = new EvaluationResult(Threshold.BELOW, 9);
        proposed = new EvaluationResult(Threshold.ABOVE, 11);
        impact = proposed.compare(original);
        assertEquals("BELOW->ABOVE should be a negative impact", Impact.NEGATIVE, impact);

        original = new EvaluationResult(Threshold.ABOVE, 11);
        proposed = new EvaluationResult(Threshold.BELOW, 9);
        impact = proposed.compare(original);
        assertEquals("ABOVE->BELOW should be a negative impact", Impact.NEGATIVE, impact);
    }
}
