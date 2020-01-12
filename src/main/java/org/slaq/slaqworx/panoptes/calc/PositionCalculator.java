package org.slaq.slaqworx.panoptes.calc;

import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;

/**
 * {@code PositionCalculator} is the parent of classes which perform calculations on
 * {@code Position}s, typically to be employed in the implementation of {@code Rule}s.
 * <p>
 * Note that a {@code PositionCalculator} may employ a {@code Position} filters, for example to
 * compute a concentration of {@code Position}s matching the filter in proportion to an entire
 * {@code Portfolio}. This should not be confused with a {@code Rule}-level {@code Position} filter
 * which, while employing an identical mechanism, serves the purpose of limiting {@code Rule}
 * evaluations to a subset of a {@code Portfolio}'s {@code Position}s.
 *
 * @author jeremy
 * @param <T>
 *            the type on which the calculator can operate
 */
public abstract class PositionCalculator<T> {
    private final SecurityAttribute<T> calculationAttribute;

    /**
     * Creates a new {@code PositionCalculator} which operates on the given
     * {@code SecurityAttribute}. This may be {@code null} if the concrete implementation does not
     * use a calculation attribute.
     *
     * @param calculationAttribute
     *            the (possibly {@code null}) {@code SecurityAttriute} type on which to calculate
     */
    protected PositionCalculator(SecurityAttribute<T> calculationAttribute) {
        this.calculationAttribute = calculationAttribute;
    }

    /**
     * Performs a calculation against the given {@code Position}s.
     *
     * @param positions
     *            the {@code Position}s on which to perform a calculation, within a given
     *            {@code PositionEvaluationContext}
     * @return the result of the calculation
     */
    public abstract double calculate(Stream<PositionEvaluationContext> positions);

    /**
     * Obtains the {@code SecurityAttribute} type to be used in calculations.
     *
     * @return a (possibly {@code null}) {@code SecurityAttribute}
     */
    protected SecurityAttribute<T> getCalculationAttribute() {
        return calculationAttribute;
    }
}
