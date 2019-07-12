package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * A ValueRule stipulates limits on values that can be calculated on a portfolio's composition,
 * either in absolute terms or relative to a benchmark.
 *
 * @author jeremy
 */
public abstract class ValueRule extends Rule {
    private final Predicate<Position> positionFilter;
    private final SecurityAttribute<Double> calculationAttribute;
    private final Double lowerLimit;
    private final Double upperLimit;

    /**
     * Creates a new ValueRule with the given parameters.
     *
     * @param id
     *            the unique ID of this rule
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
    protected ValueRule(String id, String description, Predicate<Position> positionFilter,
            SecurityAttribute<Double> calculationAttribute, Double lowerLimit, Double upperLimit,
            EvaluationGroupClassifier groupClassifier) {
        super(id, description, groupClassifier);
        this.positionFilter = positionFilter;
        this.calculationAttribute = calculationAttribute;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    @Override
    protected final EvaluationResult eval(PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions, EvaluationContext evaluationContext) {
        double value = getValue(portfolioPositions);
        if (benchmarkPositions != null) {
            // Caching the previously-calculated benchmark value should theoretically provide a
            // performance benefit to trade evaluation, as the Rule is evaluated against the current
            // Portfolio composition and again with the proposed (post-trade) composition, but the
            // benchmark value will not have changed. The measured performance difference, however,
            // seems to be a wash.
            Double previousBenchmarkValue = evaluationContext.getPreviousBenchmarkValue(this);
            double benchmarkValue;
            if (previousBenchmarkValue == null) {
                benchmarkValue = getValue(benchmarkPositions);
                evaluationContext.setPreviousBenchmarkValue(this, benchmarkValue);
            } else {
                benchmarkValue = previousBenchmarkValue;
            }
            // rescale the value to the benchmark; this may result in NaN, which means that the
            // Position's portfolio concentration is infinitely greater than the benchmark
            value /= benchmarkValue;
        }

        if (lowerLimit != null && (value != Double.NaN && value < lowerLimit)) {
            return new EvaluationResult(false);
        }

        if (upperLimit != null && (value == Double.NaN || value > upperLimit)) {
            return new EvaluationResult(false);
        }

        return new EvaluationResult(true);
    }

    /**
     * Obtains this rule's (possibly null) calculation attribute.
     *
     * @return the SecurityAttribute on which to perform calculations
     */
    protected SecurityAttribute<Double> getCalculationAttribute() {
        return calculationAttribute;
    }

    /**
     * Obtains this rule's (possibly null) position filter.
     *
     * @return the Predicate encoding the position filter
     */
    protected Predicate<Position> getPositionFilter() {
        return positionFilter;
    }

    /**
     * Evaluates the Rule calculation on the given Positions (which may be the Portfolio being
     * evaluated, or its related benchmark).
     *
     * @param positions
     *            the Positions on which to perform the appropriate calculations
     * @return the calculation result
     */
    protected abstract double getValue(PositionSupplier positions);
}
