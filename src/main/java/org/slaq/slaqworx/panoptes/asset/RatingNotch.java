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

	public RatingNotch(String symbol, double lower) {
		this.symbol = symbol;
		this.lower = lower;
	}

	@Override
	public int compareTo(RatingNotch other) {
		return Double.compare(lower, other.lower);
	}

	public double getLower() {
		return lower;
	}

	public double getMiddle() {
		return middle;
	}

	public String getSymbol() {
		return symbol;
	}

	protected void setMiddle(double middle) {
		this.middle = middle;
	}
}
