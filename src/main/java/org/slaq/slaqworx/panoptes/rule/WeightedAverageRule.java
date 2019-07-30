package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.calc.WeightedAveragePositionCalculator;

/**
 * A WeightedAverageRule stipulates limits on a calculated value, based on its weighted average
 * within a portfolio's composition, either in absolute terms or relative to a benchmark. Examples
 * of absolute rules include:
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
public class WeightedAverageRule extends ValueRule {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new WeightedAverageRule with the given parameters.
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
    public WeightedAverageRule(RuleKey key, String description, Predicate<Position> positionFilter,
            SecurityAttribute<Double> calculationAttribute, Double lowerLimit, Double upperLimit,
            EvaluationGroupClassifier groupClassifier) {
        super(key, description, positionFilter, calculationAttribute, lowerLimit, upperLimit,
                groupClassifier);
    }

    @Override
    protected double getValue(PositionSupplier positions, EvaluationContext evaluationContext) {
        WeightedAveragePositionCalculator calculator =
                new WeightedAveragePositionCalculator(getCalculationAttribute());

        return calculator.calculate(positions, getPositionFilter(), evaluationContext);
    }
}
