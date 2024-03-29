package org.slaq.slaqworx.panoptes.asset;

import javax.annotation.Nonnull;

/**
 * Describes an individual step of a {@link RatingScale}.
 *
 * @author jeremy
 */
public class RatingNotch implements Comparable<RatingNotch> {
  @Nonnull private final String symbol;
  private final double lower;
  private double middle;
  private int ordinal;

  /**
   * Creates a new {@link RatingNotch} with the given symbol and lower bound.
   *
   * @param symbol the rating symbol (e.g. AAA, Baa2)
   * @param lower the lower bound of the notch
   */
  public RatingNotch(@Nonnull String symbol, double lower) {
    this.symbol = symbol;
    this.lower = lower;
  }

  @Override
  public int compareTo(@Nonnull RatingNotch other) {
    return Double.compare(lower, other.lower);
  }

  /**
   * Obtains the lower bound of this notch.
   *
   * @return the lower bound
   */
  public double getLower() {
    return lower;
  }

  /**
   * Obtains the middle value of this notch, calculated as the average of this notch's lower bound
   * and the lower bound of the next-highest notch (or the top of the {@link RatingScale}).
   *
   * @return the middle value
   */
  public double getMiddle() {
    return middle;
  }

  /**
   * Sets the (calculated) middle value of this notch. Normally should be set only by {@link
   * RatingScale} during creation.
   *
   * @param middle the middle value of the notch
   */
  protected void setMiddle(double middle) {
    this.middle = middle;
  }

  /**
   * Obtains the ordinal value of this notch in its assigned {@link RatingScale}.
   *
   * @return the ordinal/index of this notch within its {@link RatingScale}
   */
  public int getOrdinal() {
    return ordinal;
  }

  /**
   * Sets the ordinal value of this notch in its assigned {@link RatingScale}.
   *
   * @param ordinal the ordinal/index of this notch within its {@link RatingScale}
   */
  protected void setOrdinal(int ordinal) {
    this.ordinal = ordinal;
  }

  /**
   * Obtains the symbol associated with this notch.
   *
   * @return the symbol
   */
  @Nonnull
  public String getSymbol() {
    return symbol;
  }
}
