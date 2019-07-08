package org.slaq.slaqworx.panoptes.calc;

import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * PositionCalculator is the parent of classes which perform calculations on Streams of Positions.
 *
 * @author jeremy
 *
 * @param <T>
 *            the type on which the calculator can operate
 */
public abstract class PositionCalculator<T> {
	private final SecurityAttribute<T> compareAttribute;

	protected PositionCalculator(SecurityAttribute<T> compareAttribute) {
		this.compareAttribute = compareAttribute;
	}

	public final double calculate(Portfolio portfolio) {
		return calculate(portfolio, null);
	}

	public final double calculate(Portfolio portfolio, Predicate<? super Position> positionFilter) {
		return calc(portfolio, positionFilter == null ? p -> true : positionFilter);
	}

	protected abstract double calc(Portfolio portfolio, Predicate<? super Position> positionFilter);

	protected SecurityAttribute<T> getCompareAttribute() {
		return compareAttribute;
	}
}
