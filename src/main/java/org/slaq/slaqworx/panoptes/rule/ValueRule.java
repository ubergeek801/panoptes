package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.calc.WeightedAveragePositionCalculator;

/**
 * A LimitRule stipulates limits on values that can be calculated on a portfolio's composition,
 * either in absolute terms or relative to a benchmark.
 * 
 * @author jeremy
 */
public class ValueRule extends Rule {
    private final Predicate<Position> positionFilter;
    private final SecurityAttribute<Double> calculationAttribute;
    private final Double lowerLimit;
    private final Double upperLimit;

    /**
     * Obtains this rule's position filter.
     * 
     * @return the Predicate encoding the position filter
     */
    protected Predicate<Position> getPositionFilter() {
        return positionFilter;
    }

    /**
     * 
     * @return
     */
    protected SecurityAttribute<Double> getCalculationAttribute() {
        return calculationAttribute;
    }

    /**
     * Creates a new ValueRule with the given ID, description, filter, calculation attribute, lower
     * and upper limit.
     *
     * @param id                   the unique ID of this rule
     * @param description          the rule description
     * @param positionFilter       the (possibly null) filter to be applied to Positions
     * @param calculationAttribute the attribute on which to calculate
     * @param lowerLimit           the lower limit of acceptable concentration values
     * @param upperLimit           the upper limit of acceptable concentration values
     */
    public ValueRule(String id, String description, Predicate<Position> positionFilter,
            SecurityAttribute<Double> calculationAttribute, Double lowerLimit, Double upperLimit) {
        super(id, description);
        this.positionFilter = positionFilter;
        this.calculationAttribute = calculationAttribute;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    /**
     * Evaluates the Rule on the given Portfolio, optionally relative to a given benchmark.
     *
     * @param portfolio the Portfolio on which to evaluate the Rule
     * @param benchmark the (possibly null) benchmark to evaluate relative to
     * @return true if the Rule passes, false if it fails
     */
    protected final boolean eval(Portfolio portfolio, Portfolio benchmark) {
        double value = getValue(portfolio);
        if (benchmark != null) {
            double benchmarkValue = getValue(benchmark);
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
     * Evaluates the Rule calculation on the given Portfolio (which may be the Portfolio being
     * evaluated, or its related benchmark).
     * 
     * @param portfolio the Portfolio on which to perform the appropriate calculations
     * @return the calculation result
     */
    protected double getValue(Portfolio portfolio) {
        WeightedAveragePositionCalculator calculator = new WeightedAveragePositionCalculator(
                getCalculationAttribute());

        return calculator.calculate(portfolio, getPositionFilter());
    }
}
