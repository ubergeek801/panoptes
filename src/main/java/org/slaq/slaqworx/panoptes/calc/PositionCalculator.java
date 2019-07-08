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
 * @param <T> the type on which the calculator can operate
 */
public abstract class PositionCalculator<T> {
    private final SecurityAttribute<T> calculationAttribute;

    /**
     * Creates a new PositionCalculator which operates on the given SecurityAttribute. This may be
     * null if the concrete implementation does not use a calculation attribute.
     * 
     * @param calculationAttribute the (possibly null) SecurityAttriute type on which to calculate
     */
    protected PositionCalculator(SecurityAttribute<T> calculationAttribute) {
        this.calculationAttribute = calculationAttribute;
    }

    /**
     * Performs a calculation against the given Portfolio, using no filter.
     * 
     * @param portfolio the Portfolio on which to perform a calculation
     * @return the result of the calculation
     */
    public final double calculate(Portfolio portfolio) {
        return calculate(portfolio, null);
    }

    /**
     * Performs a calculation against the given Portfolio, first applying the given filter on its
     * Positions.
     * 
     * @param portfolio      the Portfolio on which to perform a calculation
     * @param positionFilter the (possibly null) filter to be applied
     * @return the result of the calculation
     */
    public final double calculate(Portfolio portfolio, Predicate<? super Position> positionFilter) {
        return calc(portfolio, positionFilter == null ? p -> true : positionFilter);
    }

    /**
     * Performs a calculation against the given Portfolio, first applying the given filter on its
     * Positions. Public calculate() methods ultimately delegate to this method.
     * 
     * @param portfolio      the Portfolio on which to perform a calculation
     * @param positionFilter the (never null) filter to be applied
     * @return the result of the calculation
     */
    protected abstract double calc(Portfolio portfolio, Predicate<? super Position> positionFilter);

    /**
     * Obtains the SecurityAttribute type to be used in calculations.
     * 
     * @return a (possibly null) SecurityAttribute
     */
    protected SecurityAttribute<T> getCalculationAttribute() {
        return calculationAttribute;
    }
}
