package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.calc.TotalAmountPositionCalculator;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

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
    static class Configuration {
        public Double lowerLimit;
        public Double upperLimit;
    }

    /**
     * Creates a new ConcentrationRule with the given JSON configuration, key, description, filter
     * and classifier.
     *
     * @param jsonConfiguration
     *            the JSON configuration specifying lower and upper limits
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
    public static ConcentrationRule fromJson(String jsonConfiguration, RuleKey key,
            String description, String groovyFilter, EvaluationGroupClassifier groupClassifier) {
        Configuration configuration;
        try {
            configuration = JsonConfigurable.defaultObjectMapper().readValue(jsonConfiguration,
                    Configuration.class);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not parse JSON configuration " + jsonConfiguration,
                    e);
        }

        return new ConcentrationRule(key, description,
                (groovyFilter == null ? null : new GroovyPositionFilter(groovyFilter)),
                configuration.lowerLimit, configuration.upperLimit, groupClassifier);
    }

    /**
     * Creates a new ConcentrationRule with the given key, description, filter, lower and upper
     * limit.
     *
     * @param key
     *            the unique key of this rule, or null to generate one
     * @param description
     *            the rule description
     * @param positionFilter
     *            the filter to be applied to Positions to determine concentration
     * @param lowerLimit
     *            the lower limit of acceptable concentration values
     * @param upperLimit
     *            the upper limit of acceptable concentration values
     * @param groupClassifier
     *            the (possibly null) EvaluationGroupClassifier to use, which may also implement
     *            GroupAggregator
     */
    public ConcentrationRule(RuleKey key, String description,
            Predicate<PositionEvaluationContext> positionFilter, Double lowerLimit,
            Double upperLimit, EvaluationGroupClassifier groupClassifier) {
        super(key, description, positionFilter, null, lowerLimit, upperLimit, groupClassifier);
    }

    @Override
    public String getJsonConfiguration() {
        Configuration configuration = new Configuration();
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
        // ConcentrationRule works like a ValueRule in which the calculated value is scaled by the
        // total amount of the portfolio. (Eventually this could support scaling by other aggregate
        // Portfolio attributes.) Note that this requires that the specified PositionSupplier must
        // have a related Portfolio.

        TotalAmountPositionCalculator calculator = new TotalAmountPositionCalculator();

        double subtotalAmount =
                calculator.calculate(positions, getPositionFilter(), evaluationContext);
        double totalAmount = positions.getPortfolio().getTotalAmount();
        return subtotalAmount / totalAmount;
    }
}
