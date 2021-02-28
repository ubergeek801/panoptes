package org.slaq.slaqworx.panoptes.rule;

import java.util.ArrayList;
import java.util.function.Predicate;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;

/**
 * A {@link Rule} which stipulates limits on values that can be calculated on a {@link Portfolio}'s
 * composition, either in absolute terms or relative to a benchmark.
 *
 * @author jeremy
 */
public abstract class LimitRule extends GenericRule implements ConfigurableRule {
  private final Predicate<PositionEvaluationContext> positionFilter;

  private final Double lowerLimit;
  private final Double upperLimit;

  /**
   * Creates a new {@link LimitRule} with the given parameters.
   *
   * @param key
   *     the unique key of this {@link Rule}, or {@code null} to generate one
   * @param description
   *     the {@link Rule} description
   * @param positionFilter
   *     the (possibly {@code null}) filter to be applied to {@link Position}s during evaluation of
   *     the {@link Rule}
   * @param lowerLimit
   *     the (inclusive) lower limit of acceptable concentration values
   * @param upperLimit
   *     the (inclusive) upper limit of acceptable concentration values
   * @param groupClassifier
   *     the (possibly {@code null}) {@link EvaluationGroupClassifier} to use, which may also
   *     implement {@link GroupAggregator}
   */
  protected LimitRule(RuleKey key, String description,
      Predicate<PositionEvaluationContext> positionFilter, Double lowerLimit, Double upperLimit,
      EvaluationGroupClassifier groupClassifier) {
    super(key, description, groupClassifier);
    this.positionFilter = (positionFilter == null ? (p -> true) : positionFilter);
    this.lowerLimit = lowerLimit;
    this.upperLimit = upperLimit;
  }

  @Override
  public String getGroovyFilter() {
    if (positionFilter == null) {
      return null;
    }

    if (positionFilter instanceof GroovyPositionFilter) {
      return ((GroovyPositionFilter) positionFilter).getExpression();
    }

    return null;
  }

  @Override
  public Double getLowerLimit() {
    return lowerLimit;
  }

  @Override
  public String getParameterDescription() {
    ArrayList<String> descriptions = new ArrayList<>();
    if (positionFilter != null && positionFilter instanceof GroovyPositionFilter) {
      descriptions
          .add("filter=\"" + ((GroovyPositionFilter) positionFilter).getExpression() + "\"");
    }
    if (lowerLimit != null) {
      descriptions.add("lower=" + lowerLimit);
    }
    if (upperLimit != null) {
      descriptions.add("upper=" + upperLimit);
    }

    return String.join(",", descriptions);
  }

  @Override
  public Predicate<PositionEvaluationContext> getPositionFilter() {
    return positionFilter;
  }

  @Override
  public Double getUpperLimit() {
    return upperLimit;
  }

  @Override
  public boolean isBenchmarkSupported() {
    return true;
  }

  @Override
  protected final ValueResult eval(PositionSupplier positions, EvaluationGroup evaluationGroup,
      EvaluationContext evaluationContext) {
    double value = getValue(positions, evaluationContext);

    // note that for a rule that compares against a benchmark, this will not be the "final
    // answer"; that will be determined by e.g. a BenchmarkComparator

    if (lowerLimit != null && (!Double.isNaN(value) && value < lowerLimit)) {
      return new ValueResult(Threshold.BELOW, value);
    }

    if (upperLimit != null && (Double.isNaN(value) || value > upperLimit)) {
      return new ValueResult(Threshold.ABOVE, value);
    }

    return new ValueResult(Threshold.WITHIN, value);
  }

  /**
   * Evaluates the {@link Rule} calculation on the given {@link Position}s (which may be the {@link
   * Portfolio} being evaluated, or its related benchmark).
   *
   * @param positions
   *     a supplier of the {@link Position}s on which to perform the appropriate calculations
   * @param evaluationContext
   *     the {@link EvaluationContext} under which to calculate
   *
   * @return the calculation result
   */
  protected abstract double getValue(PositionSupplier positions,
      EvaluationContext evaluationContext);
}
