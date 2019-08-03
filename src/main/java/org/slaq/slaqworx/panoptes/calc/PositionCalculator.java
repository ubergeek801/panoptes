package org.slaq.slaqworx.panoptes.calc;

import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;

/**
 * PositionCalculator is the parent of classes which perform calculations on Positions, typically to
 * be employed in the implementation of Rules.
 * <p>
 * Note that a PositionCalculator may employ a Position filters, for example to compute a
 * concentration of Positions matching the filter with respect to an entire Portfolio. This should
 * not be confused with a Rule-level Position filter which, while employing an identical mechanism,
 * serves the purpose of limiting Rule evaluations to a subset of a Portfolio's Positions.
 *
 * @author jeremy
 * @param <T>
 *            the type on which the calculator can operate
 */
public abstract class PositionCalculator<T> {
    private final SecurityAttribute<T> calculationAttribute;

    /**
     * Creates a new PositionCalculator which operates on the given SecurityAttribute. This may be
     * null if the concrete implementation does not use a calculation attribute.
     *
     * @param calculationAttribute
     *            the (possibly null) SecurityAttriute type on which to calculate
     */
    protected PositionCalculator(SecurityAttribute<T> calculationAttribute) {
        this.calculationAttribute = calculationAttribute;
    }

    /**
     * Performs a calculation against the given Positions, using no filter.
     *
     * @param positions
     *            the Positions on which to perform a calculation
     * @param evaluationContext
     *            the EvaluationContext under which to calculate
     * @return the result of the calculation
     */
    public final double calculate(PositionSupplier positions, EvaluationContext evaluationContext) {
        return calculate(positions, null, evaluationContext);
    }

    /**
     * Performs a calculation against the given Positions, first applying the given filter.
     *
     * @param positions
     *            the Positions on which to perform a calculation
     * @param positionFilter
     *            the (possibly null) filter to be applied
     * @param evaluationContext
     *            the EvaluationContext under which to calculate
     * @return the result of the calculation
     */
    public final double calculate(PositionSupplier positions,
            Predicate<PositionEvaluationContext> positionFilter,
            EvaluationContext evaluationContext) {
        return calc(positions, positionFilter == null ? p -> true : positionFilter,
                evaluationContext);
    }

    /**
     * Performs a calculation against the given Positions, first applying the given filter. Public
     * calculate() methods ultimately delegate to this method.
     *
     * @param positions
     *            the Positions on which to perform a calculation
     * @param positionFilter
     *            the (never null) filter to be applied
     * @param evaluationContext
     *            the EvaluationContext under which to calculate
     * @return the result of the calculation
     */
    protected abstract double calc(PositionSupplier positions,
            Predicate<PositionEvaluationContext> positionFilter,
            EvaluationContext evaluationContext);

    /**
     * Obtains the SecurityAttribute type to be used in calculations.
     *
     * @return a (possibly null) SecurityAttribute
     */
    protected SecurityAttribute<T> getCalculationAttribute() {
        return calculationAttribute;
    }
}
