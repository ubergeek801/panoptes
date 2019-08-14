package org.slaq.slaqworx.panoptes.asset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * A {@code RatingScale} encapsulates an ordered list of {@code RatingNotch}es which are used by a
 * particular rater, and provides operations to convert between numeric and symbolic ratings within
 * the scale.
 *
 * @author jeremy
 */
public class RatingScale {
    private final Double[] notchValues;
    private final ArrayList<RatingNotch> notches;
    private final HashMap<String, RatingNotch> symbolRatingMap;

    /**
     * Creates a new {@code RatingScale} of the given notches and the given maximum scale value.
     *
     * @param ratings
     *            the {@code RatingNotch}es (in no particular order) comprising this
     *            {@code RatingScale}
     * @param max
     *            the maximum value of the {@code RatingScale}
     */
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

    /**
     * Obtains the {@code RatingNotch} corresponding to the given rating value: that is, notch
     * <i>i</i> such that <i>i</i>.lower <= value < (<i>i</i> + 1).lower. If value is lower or
     * higher than the minimum or maximum value of the scale, the lowest or highest
     * {@code RatingNotch}, respectively, is returned.
     *
     * @param value
     *            the value for which to find the corresponding {@code RatingNotch}
     * @return the {@code RatingNotch} corresponding to the given value
     */
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

    /**
     * Obtains the {@code RatingNotch} corresponding to the given symbol.
     *
     * @param symbol
     *            the symbol for which to find the {@code RatingNotch}
     * @return the {@code RatingNotch} corresponding to the given symbol, or null if it does not
     *         exist
     */
    public RatingNotch getRatingNotch(String symbol) {
        return symbolRatingMap.get(symbol);
    }
}
