package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * {@code RatingScaleTest} tests the functionality of the {@code RatingScale}.
 *
 * @author jeremy
 */
public class RatingScaleTest {
    /**
     * Tests that {@code getRatingNotch()} behaves as expected.
     */
    @Test
    public void testGetRatingNotch() {
        ArrayList<RatingNotch> ratingNotches = new ArrayList<>();
        ratingNotches.add(new RatingNotch("A", 9));
        ratingNotches.add(new RatingNotch("B", 8));
        ratingNotches.add(new RatingNotch("C", 7));
        ratingNotches.add(new RatingNotch("D", 6));
        ratingNotches.add(new RatingNotch("E", 5));
        ratingNotches.add(new RatingNotch("F", 0));
        RatingScale scale = new RatingScale(ratingNotches, 10);

        // a rating below the scale should translate to the minimum rating
        assertEquals("F", scale.getRatingNotch(-1).getSymbol(), "unexpected below-scale rating");
        // as should the very bottom rating
        assertEquals("F", scale.getRatingNotch(0).getSymbol(), "unexpected bottom-scale rating");
        // a handful of mid-scale ratings
        assertEquals("F", scale.getRatingNotch(0.5).getSymbol(), "unexpected rating for 0.5");
        assertEquals("E", scale.getRatingNotch(5).getSymbol(), "unexpected rating for 5");
        assertEquals("B", scale.getRatingNotch(8.5).getSymbol(), "unexpected rating for 8.5");
        // ratings at the top and above the scale should translate to the maximum rating
        assertEquals("A", scale.getRatingNotch(10).getSymbol(), "unexpected top-scale rating");
        assertEquals("A", scale.getRatingNotch(10.25).getSymbol(), "unexpected above-scale rating");

        // also test symbol lookups

        RatingNotch notch = scale.getRatingNotch("B");
        assertEquals("B", notch.getSymbol(), "unexpected symbol for notch B");
        assertEquals(8, notch.getLower(), TestUtil.EPSILON, "unexpected lower value for notch B");
        assertEquals(8.5, notch.getMiddle(), TestUtil.EPSILON,
                "unexpected middle value for notch B");

        assertNull(scale.getRatingNotch("Q"), "should not have found notch for bogus symbol");

        // finally test ordinal values

        assertEquals(0, scale.getRatingNotch("F").getOrdinal(), "unexpected ordinal for rating F");
        assertEquals(1, scale.getRatingNotch("E").getOrdinal(), "unexpected ordinal for rating E");
        assertEquals(2, scale.getRatingNotch("D").getOrdinal(), "unexpected ordinal for rating D");
        assertEquals(3, scale.getRatingNotch("C").getOrdinal(), "unexpected ordinal for rating C");
        assertEquals(4, scale.getRatingNotch("B").getOrdinal(), "unexpected ordinal for rating B");
        assertEquals(5, scale.getRatingNotch("A").getOrdinal(), "unexpected ordinal for rating A");
    }
}
