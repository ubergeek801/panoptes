package org.slaq.slaqworx.panoptes.rule;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.RuleResult.Threshold;

/**
 * A {@code ValueRule} stipulates limits on values that can be calculated on a {@code Portfolio}'s
 * composition, either in absolute terms or relative to a benchmark.
 *
 * @author jeremy
 */
public abstract class ValueRule extends GenericRule implements ConfigurableRule {
    private final Predicate<PositionEvaluationContext> positionFilter;
    private final SecurityAttribute<Double> calculationAttribute;
    private final Double lowerLimit;
    private final Double upperLimit;

    /**
     * Creates a new {@code ValueRule} with the given parameters.
     *
     * @param key
     *            the unique key of this {@code Rule}, or {@code null} to generate one
     * @param description
     *            the {@code Rule} description
     * @param positionFilter
     *            the (possibly {@code null}) filter to be applied to {@code Position}s during
     *            evaluation of the {@code Rule}
     * @param calculationAttribute
     *            the attribute on which to calculate
     * @param lowerLimit
     *            the lower limit of acceptable concentration values
     * @param upperLimit
     *            the upper limit of acceptable concentration values
     * @param groupClassifier
     *            the (possibly {@code null}) {@code EvaluationGroupClassifier} to use, which may
     *            also implement {@code GroupAggregator}
     */
    protected ValueRule(RuleKey key, String description,
            Predicate<PositionEvaluationContext> positionFilter,
            SecurityAttribute<Double> calculationAttribute, Double lowerLimit, Double upperLimit,
            EvaluationGroupClassifier groupClassifier) {
        super(key, description, groupClassifier);
        this.positionFilter = (positionFilter == null ? (p -> true) : positionFilter);
        this.calculationAttribute = calculationAttribute;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    @Override
    public String getGroovyFilter() {
        if (positionFilter == null) {
            return null;
        }

        if (positionFilter instanceof GroovyPositionFilter) {
            return ((GroovyPositionFilter)positionFilter).getExpression();
        }

        return null;
    }

    @Override
    public String getParameterDescription() {
        ArrayList<String> descriptions = new ArrayList<>();
        if (positionFilter != null && positionFilter instanceof GroovyPositionFilter) {
            descriptions.add(
                    "filter=\"" + ((GroovyPositionFilter)positionFilter).getExpression() + "\"");
        }
        if (calculationAttribute != null) {
            descriptions.add("attribute=\"" + calculationAttribute.getName() + "\"");
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
    protected final RuleResult eval(PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions, EvaluationContext evaluationContext) {
        double value = getValue(portfolioPositions, evaluationContext);
        double scaledValue;
        Double benchmarkValue;
        if (benchmarkPositions != null) {
            benchmarkValue = getValue(benchmarkPositions, evaluationContext);
            // rescale the value to the benchmark; this may result in NaN, which means that the
            // Position's portfolio concentration is infinitely greater than the benchmark
            scaledValue = value / benchmarkValue;
        } else {
            benchmarkValue = null;
            scaledValue = value;
        }

        if (lowerLimit != null && (scaledValue != Double.NaN && scaledValue < lowerLimit)) {
            return new RuleResult(Threshold.BELOW, value, benchmarkValue);
        }

        if (upperLimit != null && (scaledValue == Double.NaN || scaledValue > upperLimit)) {
            return new RuleResult(Threshold.ABOVE, value, benchmarkValue);
        }

        return new RuleResult(Threshold.WITHIN, value, benchmarkValue);
    }

    /**
     * Obtains this {@code Rule}'s (possibly {@code null}) calculation attribute.
     *
     * @return the {@code SecurityAttribute} on which to perform calculations
     */
    protected SecurityAttribute<Double> getCalculationAttribute() {
        return calculationAttribute;
    }

    /**
     * Obtains this {@code Rule}'s (possibly {@code null}) lower limit.
     *
     * @return the lower limit against which to evaluate
     */
    protected Double getLowerLimit() {
        return lowerLimit;
    }

    /**
     * Obtains this {@code Rule}'s (possibly {@code null}) upper limit.
     *
     * @return the upper limit against which to evaluate
     */
    protected Double getUpperLimit() {
        return upperLimit;
    }

    /**
     * Evaluates the {@code Rule} calculation on the given {@code Position}s (which may be the
     * {@code Portfolio} being evaluated, or its related benchmark).
     *
     * @param positions
     *            a supplier of the {@code Position}s on which to perform the appropriate
     *            calculations
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to calculate
     * @return the calculation result
     */
    protected abstract double getValue(PositionSupplier positions,
            EvaluationContext evaluationContext);
}
