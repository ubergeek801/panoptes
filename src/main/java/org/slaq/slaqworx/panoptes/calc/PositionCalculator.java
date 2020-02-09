package org.slaq.slaqworx.panoptes.calc;

import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;

/**
 * {@code PositionCalculator} is the parent of classes which perform calculations on
 * {@code Position}s, typically to be employed in the implementation of {@code Rule}s.
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

    /**
     * Obtains the {@code Double} value corresponding to the given attribute value.
     *
     * @param attributeValue
     *            the attribute value for which to obtain a corresponding {@code Double} value
     * @param evaluationContext
     *            the {@code EvaluationContext} in which the evaluation is occurring
     * @return the interpreted value of the given attribute value
     */
    protected Double getValue(T attributeValue, EvaluationContext evaluationContext) {
        return (calculationAttribute == null ? null
                : calculationAttribute.getValueProvider().apply(attributeValue, evaluationContext));
    }
}
