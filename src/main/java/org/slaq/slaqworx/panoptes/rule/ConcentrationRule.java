package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.calc.TotalAmountPositionCalculator;

/**
 * A ConcentrationRule stipulates limits on portfolio concentration in Securities matched by a given
 * Position filter, either in absolute terms or relative to a benchmark. Examples of absolute rules
 * include:
 * <ul>
 * <li>portfolio holdings in Securities from the Emerging Markets region may not exceed 10%
 * <li>portfolio holdings in US-domiciled Securities must be at least 50%
 * </ul>
 * Examples of benchmark-relative rules include:
 * <ul>
 * <li>portfolio holdings in BRL-denominated Securities must be between 95% and 105% of the
 * benchmark
 * <li>portfolio holdings in Securities with duration < 5.0 must be less than 80% of the benchmark
 * </ul>
 *
 * @author jeremy
 */
public class ConcentrationRule extends ValueRule {
    /**
     * Creates a new ConcentrationRule with the given ID, description, filter, lower and upper
     * limit.
     *
     * @param id
     *            the unique ID of this rule
     * @param description
     *            the rule description
     * @param positionFilter
     *            the filter to be applied to Positions to determine concentration
     * @param lowerLimit
     *            the lower limit of acceptable concentration values
     * @param upperLimit
     *            the upper limit of acceptable concentration values
     * @param evaluationGroup
     *            the (possibly null) EvaluationGroup to use
     */
    public ConcentrationRule(String id, String description, Predicate<Position> positionFilter,
            Double lowerLimit, Double upperLimit, EvaluationGroup evaluationGroup) {
        super(id, description, positionFilter, null, lowerLimit, upperLimit, evaluationGroup);
    }

    @Override
    protected double getValue(Portfolio portfolio) {
        // ConcentrationRule works like a ValueRule in which the calculated value is scaled by the
        // total amount of the Portfolio. (Eventually this could support scaling by other aggregate
        // Portfolio attributes.)

        TotalAmountPositionCalculator calculator = new TotalAmountPositionCalculator();

        double subtotalAmount = calculator.calculate(portfolio, getPositionFilter());
        double totalAmount = portfolio.getTotalAmount();
        return subtotalAmount / totalAmount;
    }
}
