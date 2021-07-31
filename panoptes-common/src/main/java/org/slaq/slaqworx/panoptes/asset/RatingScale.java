package org.slaq.slaqworx.panoptes.asset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import javax.annotation.Nonnull;

/**
 * Encapsulates an ordered list of {@link RatingNotch}es which are used by a particular rater, and
 * provides operations to convert between numeric and symbolic ratings within the scale.
 *
 * @author jeremy
 */
public class RatingScale {
  @Nonnull
  private static final RatingScale defaultScale;

  static {
    // these rating symbols are used in the PIMCO benchmarks; the numeric equivalents are a
    // fabrication
    ArrayList<RatingNotch> notches = new ArrayList<>();
    notches.add(new RatingNotch("AAA", 97));
    notches.add(new RatingNotch("AA1", 94));
    notches.add(new RatingNotch("AA2", 91));
    notches.add(new RatingNotch("AA3", 88));
    notches.add(new RatingNotch("A1", 85));
    notches.add(new RatingNotch("A2", 82));
    notches.add(new RatingNotch("A3", 79));
    notches.add(new RatingNotch("BBB1", 76));
    notches.add(new RatingNotch("BBB2", 73));
    notches.add(new RatingNotch("BBB3", 70));
    notches.add(new RatingNotch("BB1", 67));
    notches.add(new RatingNotch("BB2", 64));
    notches.add(new RatingNotch("BB3", 61));
    notches.add(new RatingNotch("B1", 58));
    notches.add(new RatingNotch("B2", 55));
    notches.add(new RatingNotch("B3", 52));
    notches.add(new RatingNotch("CCC1", 49));
    notches.add(new RatingNotch("CCC2", 46));
    notches.add(new RatingNotch("CCC3", 43));
    notches.add(new RatingNotch("CC", 40));
    notches.add(new RatingNotch("C", 37));
    notches.add(new RatingNotch("D", 0));
    defaultScale = new RatingScale(notches, 100);
  }

  @Nonnull
  private final double[] notchValues;
  @Nonnull
  private final ArrayList<RatingNotch> notches;
  @Nonnull
  private final HashMap<String, RatingNotch> symbolRatingMap;

  /**
   * Creates a new {@link RatingScale} of the given notches and the given maximum scale value.
   *
   * @param ratings
   *     the {@link RatingNotch}es (in no particular order) comprising this {@link RatingScale}
   * @param max
   *     the maximum value of the {@link RatingScale}
   */
  public RatingScale(@Nonnull Collection<RatingNotch> ratings, double max) {
    notches = new ArrayList<>(ratings);
    Collections.sort(notches);

    notchValues = new double[notches.size()];

    symbolRatingMap = new HashMap<>(notches.size());

    // set the ordinal and middle values for each notch while also building symbolRatingMap
    for (int i = 0; i < notchValues.length; i++) {
      RatingNotch rating = notches.get(i);
      rating.setOrdinal(i);
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
   * Obtains the default rating scale which is, so far, the only {@link RatingScale}.
   *
   * @return the default {@link RatingScale}
   */
  @Nonnull
  public static RatingScale defaultScale() {
    return defaultScale;
  }

  /**
   * Obtains the {@link RatingNotch} corresponding to the given rating value: that is, notch
   * <i>i</i> such that <i>i</i>.lower &lt;= value &lt; (<i>i</i> + 1).lower. If value is lower or
   * higher than the minimum or maximum value of the scale, the lowest or highest {@link
   * RatingNotch}, respectively, is returned.
   *
   * @param value
   *     the value for which to find the corresponding {@link RatingNotch}
   *
   * @return the {@link RatingNotch} corresponding to the given value
   */
  @Nonnull
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
   * Obtains the {@link RatingNotch} corresponding to the given symbol.
   *
   * @param symbol
   *     the symbol for which to find the {@link RatingNotch}
   *
   * @return the {@link RatingNotch} corresponding to the given symbol, or {@code null} if it does
   *     not exist
   */
  public RatingNotch getRatingNotch(String symbol) {
    return symbolRatingMap.get(symbol);
  }
}
