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
     *            the (possibly null) EvaluationGroupClassifier to use
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

    /**
     * Evaluates the Rule on the given portfolio Positions, optionally relative to a given
     * benchmark.
     *
     * @param portfolioPositions
     *            the portfolio Positions on which to evaluate the Rule
     * @param benchmarkPositions
     *            the (possibly null) benchmark Positions to evaluate relative to
     * @return true if the Rule passes, false if it fails
     */
    @Override
    protected final boolean eval(PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions) {
        double value = getValue(portfolioPositions);
        if (benchmarkPositions != null) {
            double benchmarkValue = getValue(benchmarkPositions);
            // rescale the value to the benchmark; this may result in NaN, which means that the
            // portfolio concentration is infinitely greater than the benchmark
            value /= benchmarkValue;
        }

        if (lowerLimit != null && (value != Double.NaN && value < lowerLimit)) {
            return false;
        }

        if (upperLimit != null && (value == Double.NaN || value > upperLimit)) {
            return false;
        }

        return true;
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
