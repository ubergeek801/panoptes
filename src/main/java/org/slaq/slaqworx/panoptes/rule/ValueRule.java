package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult.Threshold;

/**
 * A ValueRule stipulates limits on values that can be calculated on a portfolio's composition,
 * either in absolute terms or relative to a benchmark.
 *
 * @author jeremy
 */
public abstract class ValueRule extends MaterializedRule {
    private final Predicate<PositionEvaluationContext> positionFilter;
    private final SecurityAttribute<Double> calculationAttribute;
    private final Double lowerLimit;
    private final Double upperLimit;

    /**
     * Creates a new ValueRule with the given parameters.
     *
     * @param key
     *            the unique key of this rule, or null to generate one
     * @param description
     *            the rule description
     * @param positionFilter
     *            the (possibly null) filter to be applied to Positions
     * @param calculationAttribute
     *            the attribute on which to calculate
     * @param lowerLimit
     *            the lower limit of acceptable concentration values
     * @param upperLimit
     *            the upper limit of acceptable concentration values
     * @param groupClassifier
     *            the (possibly null) EvaluationGroupClassifier to use, which may also implement
     *            GroupAggregator
     */
    protected ValueRule(RuleKey key, String description,
            Predicate<PositionEvaluationContext> positionFilter,
            SecurityAttribute<Double> calculationAttribute, Double lowerLimit, Double upperLimit,
            EvaluationGroupClassifier groupClassifier) {
        super(key, description, groupClassifier);
        this.positionFilter = positionFilter;
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
    protected final EvaluationResult eval(PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions, EvaluationContext evaluationContext) {
        double value = getValue(portfolioPositions, evaluationContext);
        if (benchmarkPositions != null) {
            double benchmarkValue = getValue(benchmarkPositions, evaluationContext);
            // rescale the value to the benchmark; this may result in NaN, which means that the
            // Position's portfolio concentration is infinitely greater than the benchmark
            value /= benchmarkValue;
        }

        if (lowerLimit != null && (value != Double.NaN && value < lowerLimit)) {
            return new EvaluationResult(Threshold.BELOW, value);
        }

        if (upperLimit != null && (value == Double.NaN || value > upperLimit)) {
            return new EvaluationResult(Threshold.ABOVE, value);
        }

        return new EvaluationResult(Threshold.WITHIN, value);
    }

    /**
     * Obtains this Rule's (possibly null) calculation attribute.
     *
     * @return the SecurityAttribute on which to perform calculations
     */
    protected SecurityAttribute<Double> getCalculationAttribute() {
        return calculationAttribute;
    }

    /**
     * Obtains this Rule's (possibly null) lower limit.
     *
     * @return the lower limit against which to evaluate
     */
    protected Double getLowerLimit() {
        return lowerLimit;
    }

    /**
     * Obtains this rule's (possibly null) position filter.
     *
     * @return the Predicate encoding the position filter
     */
    protected Predicate<PositionEvaluationContext> getPositionFilter() {
        return positionFilter;
    }

    /**
     * Obtains this Rule's (possibly null) upper limit.
     *
     * @return the upper limit against which to evaluate
     */
    protected Double getUpperLimit() {
        return upperLimit;
    }

    /**
     * Evaluates the Rule calculation on the given Positions (which may be the Portfolio being
     * evaluated, or its related benchmark).
     *
     * @param positions
     *            the Positions on which to perform the appropriate calculations
     * @param evaluationContext
     *            the EvaluationContext under which to calculate
     * @return the calculation result
     */
    protected abstract double getValue(PositionSupplier positions,
            EvaluationContext evaluationContext);
}
