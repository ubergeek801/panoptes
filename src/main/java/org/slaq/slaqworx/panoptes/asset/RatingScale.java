package org.slaq.slaqworx.panoptes.asset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * A RatingScale encapsulates an ordered list of RatingNotches which are used by a particular rater,
 * and provides operations to convert between numeric and symbolic ratings within the scale.
 *
 * @author jeremy
 */
public class RatingScale {
	private final Double[] notchValues;
	private final ArrayList<RatingNotch> notches;
	private final HashMap<String, RatingNotch> symbolRatingMap;

	public RatingScale(Collection<RatingNotch> ratings, double max) {
		notches = new ArrayList<>(ratings);
		Collections.sort(notches);

		notchValues = new Double[notches.size()];

		symbolRatingMap = new HashMap<>(notches.size());

		for (int i = 0; i < notchValues.length; i++) {
			RatingNotch rating = notches.get(i);
			notchValues[i] = rating.getLower();
			symbolRatingMap.put(rating.getSymbol(), rating);
			double upper;
			if (i == notchValues.length - 1) {
				upper = max;
			} else {
				upper = notches.get(i + 1).getLower();
			}
			rating.setMiddle((rating.getLower() + upper) / 2);
		}
	}

	public RatingNotch getRatingNotch(double value) {
		int index = Arrays.binarySearch(notchValues, value);

		if (index >= 0) {
			// if the index is non-negative, it indicates an exact match on the lower bound so no
			// translation is necessary
		} else {
			// if the value is negative, it is (-(insertion point) - 1), so negate
			index = (-index - 2);
		}

		// if index is off the scale, just use the lowest/highest value
		index = Math.max(index, 0);
		index = Math.min(index, notchValues.length - 1);

		return notches.get(index);
	}

	public RatingNotch getRatingNotch(String symbol) {
		return symbolRatingMap.get(symbol);
	}
}
