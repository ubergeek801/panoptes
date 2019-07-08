package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.calc.WeightedAveragePositionCalculator;

/**
 * A ValueRule stipulates limits on values that can be calculated on a portfolio's composition,
 * either in absolute terms or relative to a benchmark. Examples of absolute rules include:
 * <ul>
 * <li>the weighted average of a portfolio's duration may not exceed 5.0
 * </ul>
 * Examples of benchmark-relative rules include:
 * <ul>
 * <li>the (weighted) average quality of a portfolio must be at least 90% of the benchmark
 * </ul>
 *
 * @author jeremy
 */
public class ValueRule extends Rule {
    private final Predicate<Position> positionFilter;
    private final SecurityAttribute<Double> calculationAttribute;

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
        super(id, description, lowerLimit, upperLimit);
        this.positionFilter = positionFilter;
        this.calculationAttribute = calculationAttribute;
    }

    @Override
    protected double eval(Portfolio portfolio, Portfolio benchmark) {
        WeightedAveragePositionCalculator calculator = new WeightedAveragePositionCalculator(
                calculationAttribute);

        double attributeValue = calculator.calculate(portfolio, positionFilter);
        if (benchmark != null) {
            double benchmarkValue = calculator.calculate(benchmark);
            // rescale the value to the benchmark; this may result in NaN, which means that the
            // portfolio concentration is infinitely greater than the benchmark
            attributeValue /= benchmarkValue;
        }

        return attributeValue;
    }
}
