package org.slaq.slaqworx.panoptes.calc;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * The parent of classes which perform calculations on {@link Position}s, typically to be employed
 * in the implementation of {@link Rule}s.
 *
 * @param <T>
 *     the type on which the calculator can operate
 *
 * @author jeremy
 */
public abstract class PositionCalculator<T> {
  private final SecurityAttribute<T> calculationAttribute;

  /**
   * Creates a new {@link PositionCalculator} which operates on the given {@link SecurityAttribute}.
   * This may be {@code null} if the concrete implementation does not use a calculation attribute.
   *
   * @param calculationAttribute
   *     the (possibly {@code null}) {@link SecurityAttribute} type on which to calculate
   */
  protected PositionCalculator(SecurityAttribute<T> calculationAttribute) {
    this.calculationAttribute = calculationAttribute;
  }

  /**
   * Performs a calculation against the given {@link Position}s.
   *
   * @param positions
   *     the {@link Position}s on which to perform a calculation, within a given {@link
   *     PositionEvaluationContext}
   *
   * @return the result of the calculation
   */
  public abstract double calculate(Stream<PositionEvaluationContext> positions);

  /**
   * Obtains the {@link SecurityAttribute} type to be used in calculations.
   *
   * @return a (possibly {@code null}) {@link SecurityAttribute}
   */
  protected SecurityAttribute<T> getCalculationAttribute() {
    return calculationAttribute;
  }

  /**
   * Obtains the {@link Double} value corresponding to the given attribute value.
   *
   * @param attributeValue
   *     the attribute value for which to obtain a corresponding {@link Double} value
   * @param evaluationContext
   *     the {@link EvaluationContext} in which the evaluation is occurring
   *
   * @return the interpreted value of the given attribute value
   */
  protected Double getValue(@Nonnull T attributeValue,
      @Nonnull EvaluationContext evaluationContext) {
    return (calculationAttribute == null ? null :
        calculationAttribute.getValueProvider().apply(attributeValue, evaluationContext));
  }
}
