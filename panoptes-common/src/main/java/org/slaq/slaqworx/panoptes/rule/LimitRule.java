package org.slaq.slaqworx.panoptes.rule;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;

/**
 * A {@code Rule} which stipulates limits on values that can be calculated on a {@code Portfolio}'s
 * composition, either in absolute terms or relative to a benchmark.
 *
 * @author jeremy
 */
public abstract class LimitRule extends GenericRule implements ConfigurableRule {
    private final Predicate<PositionEvaluationContext> positionFilter;

    private final Double lowerLimit;
    private final Double upperLimit;

    /**
     * Creates a new {@code LimitRule} with the given parameters.
     *
     * @param key
     *            the unique key of this {@code Rule}, or {@code null} to generate one
     * @param description
     *            the {@code Rule} description
     * @param positionFilter
     *            the (possibly {@code null}) filter to be applied to {@code Position}s during
     *            evaluation of the {@code Rule}
     * @param lowerLimit
     *            the lower limit of acceptable concentration values
     * @param upperLimit
     *            the upper limit of acceptable concentration values
     * @param groupClassifier
     *            the (possibly {@code null}) {@code EvaluationGroupClassifier} to use, which may
     *            also implement {@code GroupAggregator}
     */
    protected LimitRule(RuleKey key, String description,
            Predicate<PositionEvaluationContext> positionFilter, Double lowerLimit,
            Double upperLimit, EvaluationGroupClassifier groupClassifier) {
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
            return ((GroovyPositionFilter)positionFilter).getExpression();
        }

        return null;
    }

    @Override
    public String getJsonConfiguration() {
        // TODO Auto-generated method stub
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
            descriptions.add(
                    "filter=\"" + ((GroovyPositionFilter)positionFilter).getExpression() + "\"");
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

        if (lowerLimit != null && (value != Double.NaN && value < lowerLimit)) {
            return new ValueResult(Threshold.BELOW, value);
        }

        if (upperLimit != null && (value == Double.NaN || value > upperLimit)) {
            return new ValueResult(Threshold.ABOVE, value);
        }

        return new ValueResult(Threshold.WITHIN, value);
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
