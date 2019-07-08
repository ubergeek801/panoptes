package org.slaq.slaqworx.panoptes.asset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.junit.Test;
import org.slaq.slaqworx.panoptes.TestUtil;

/**
 * RatingScaleTest tests the functionality of the RatingScale.
 *
 * @author jeremy
 */
public class RatingScaleTest {
	/**
	 * Tests that getRatingNotch() behaves as expected.
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
		assertEquals("unexpected below-scale rating", "F", scale.getRatingNotch(-1).getSymbol());
		// as should the very bottom rating
		assertEquals("unexpected bottom-scale rating", "F", scale.getRatingNotch(0).getSymbol());
		// a handful of mid-scale ratings
		assertEquals("unexpected rating for 0.5", "F", scale.getRatingNotch(0.5).getSymbol());
		assertEquals("unexpected rating for 5", "E", scale.getRatingNotch(5).getSymbol());
		assertEquals("unexpected rating for 8.5", "B", scale.getRatingNotch(8.5).getSymbol());
		// ratings at the top and above the scale should translate to the maximum rating
		assertEquals("unexpected top-scale rating", "A", scale.getRatingNotch(10).getSymbol());
		assertEquals("unexpected above-scale rating", "A", scale.getRatingNotch(10.25).getSymbol());

		// also test symbol lookups

		RatingNotch notch = scale.getRatingNotch("B");
		assertEquals("unexpected symbol for notch B", "B", notch.getSymbol());
		assertEquals("unexpected lower value for notch B", 8, notch.getLower(), TestUtil.EPSILON);
		assertEquals("unexpected middle value for notch B", 8.5, notch.getMiddle(),
				TestUtil.EPSILON);

		assertNull("should not have found notch for bogus symbol", scale.getRatingNotch("Q"));
	}
}
