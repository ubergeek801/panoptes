package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.calc.WeightedAveragePositionCalculator;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

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
    static class Configuration {
        public String attribute;
        public Double lowerLimit;
        public Double upperLimit;
    }

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new WeightedAverageRule with the given JSON configuration, key, description, filter
     * and classifier.
     *
     * @param jsonConfiguration
     *            the JSON configuration specifying calculation attribute, lower and upper limits
     * @param securityProvider
     *            the SecurityProvider to use for the Groovy filter
     * @param key
     *            the unique key of this rule, or null to generate one
     * @param description
     *            the rule description
     * @param groovyFilter
     *            a (possibly null) Groovy expression to be used as a Position filter
     * @param groupClassifier
     *            the (possibly null) EvaluationGroupClassifier to use, which may also implement
     *            GroupAggregator
     */
    public static WeightedAverageRule fromJson(String jsonConfiguration,
            SecurityProvider securityProvider, RuleKey key, String description, String groovyFilter,
            EvaluationGroupClassifier groupClassifier) {
        Configuration configuration;
        try {
            configuration = JsonConfigurable.defaultObjectMapper().readValue(jsonConfiguration,
                    Configuration.class);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not parse JSON configuration " + jsonConfiguration,
                    e);
        }

        @SuppressWarnings("unchecked")
        SecurityAttribute<Double> calculationAttribute =
                (SecurityAttribute<Double>)SecurityAttribute.of(configuration.attribute);
        return new WeightedAverageRule(key, description,
                (groovyFilter == null ? null
                        : new GroovyPositionFilter(groovyFilter, securityProvider)),
                calculationAttribute, configuration.lowerLimit, configuration.upperLimit,
                groupClassifier);
    }

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
    public String getJsonConfiguration() {
        Configuration configuration = new Configuration();
        configuration.attribute = getCalculationAttribute().getName();
        configuration.lowerLimit = getLowerLimit();
        configuration.upperLimit = getUpperLimit();

        try {
            return JsonConfigurable.defaultObjectMapper().writeValueAsString(configuration);
        } catch (JsonProcessingException e) {
            // TODO throw a better exception
            throw new RuntimeException("could not serialize JSON configuration", e);
        }
    }

    @Override
    protected double getValue(PositionSupplier positions, EvaluationContext evaluationContext) {
        WeightedAveragePositionCalculator calculator =
                new WeightedAveragePositionCalculator(getCalculationAttribute());

        return calculator.calculate(positions, getPositionFilter(), evaluationContext);
    }
}
