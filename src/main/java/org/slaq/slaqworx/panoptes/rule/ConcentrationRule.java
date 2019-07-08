package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.calc.TotalAmountPositionCalculator;

/**
 * A ConcentrationRule stipulates limits on portfolio concentration in Securities with a particular
 * attribute, either in absolute terms or relative to a benchmark. Examples of absolute rules
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
public class ConcentrationRule extends Rule {
    private final Predicate<Position> positionFilter;

    /**
     * Creates a new ConcentrationRule with the given ID, description, filter, lower and upper
     * limit.
     * 
     * @param id             the unique ID of this rule
     * @param description    the rule description
     * @param positionFilter the (possibly null) filter to be applied to Positions
     * @param lowerLimit     the lower limit of acceptable concentration values
     * @param upperLimit     the upper limit of acceptable concentration values
     */
    public ConcentrationRule(String id, String description, Predicate<Position> positionFilter,
            Double lowerLimit, Double upperLimit) {
        super(id, description, lowerLimit, upperLimit);
        this.positionFilter = positionFilter;
    }

    @Override
    protected double eval(Portfolio portfolio, Portfolio benchmark) {
        TotalAmountPositionCalculator calculator = new TotalAmountPositionCalculator();

        double subtotalAmount = calculator.calculate(portfolio, positionFilter);
        double totalAmount = portfolio.getTotalAmount();
        double concentration = subtotalAmount / totalAmount;

        if (benchmark != null) {
            // rescale concentration to the benchmark
            double subtotalBenchmark = calculator.calculate(benchmark, positionFilter);
            double benchmarkConcentration = subtotalBenchmark / benchmark.getTotalAmount();
            // this may result in NaN, which means that the portfolio concentration is infinitely
            // greater than the benchmark
            concentration /= benchmarkConcentration;
        }

        return concentration;
    }
}
