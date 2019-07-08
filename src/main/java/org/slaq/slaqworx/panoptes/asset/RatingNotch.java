package org.slaq.slaqworx.panoptes.asset;

/**
 * A RatingNotch describes a single step of a RatingScale.
 *
 * @author jeremy
 */
public class RatingNotch implements Comparable<RatingNotch> {
    private final String symbol;
    private final double lower;
    private double middle;

    /**
     * Creates a new RatingNotch with the given symbol and lower bound.
     *
     * @param symbol the rating symbol (e.g. AAA, Baa2)
     * @param lower  the lower bound of the notch
     */
    public RatingNotch(String symbol, double lower) {
        this.symbol = symbol;
        this.lower = lower;
    }

    @Override
    public int compareTo(RatingNotch other) {
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
     * and the lower bound of the next-highest notch (or the top of the RatingScale).
     *
     * @return the middle value
     */
    public double getMiddle() {
        return middle;
    }

    /**
     * Obtains the symbol associated with this notch.
     *
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Sets the (calculated) middle value of this notch. Normally should be set only by RatingScale
     * during creation.
     *
     * @param middle the middle value of the notch
     */
    protected void setMiddle(double middle) {
        this.middle = middle;
    }
}
