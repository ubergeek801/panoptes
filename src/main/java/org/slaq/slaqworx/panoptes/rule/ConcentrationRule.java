package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.calc.TotalMarketValuePositionCalculator;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * A {@code ConcentrationRule} stipulates limits on {@code Portfolio} concentration in
 * {@code Securities} matched by a given {@code Position} filter, either in absolute terms or
 * relative to a benchmark. Examples of absolute rules include:
 * <ul>
 * <li>{@code Portfolio} holdings in {@code Securities} from the Emerging Markets region may not
 * exceed 10%
 * <li>{@code Portfolio} holdings in US-domiciled {@code Securities} must be at least 50%
 * </ul>
 * Examples of benchmark-relative rules include:
 * <ul>
 * <li>{@code Portfolio} holdings in BRL-denominated {@code Securities} must be between 95% and 105%
 * of the benchmark
 * <li>{@code Portfolio} holdings in {@code Securities} with duration < 5.0 must be less than 80% of
 * the benchmark
 * </ul>
 *
 * @author jeremy
 */
public class ConcentrationRule extends ValueRule {
    /**
     * {@code Configuration} encapsulates the properties of a {@code ConcentrationRule} which are
     * configurable via e.g. JSON.
     */
    static class Configuration {
        public Double lowerLimit;
        public Double upperLimit;
    }

    /**
     * Creates a new {@code ConcentrationRule} with the given JSON configuration, key, description,
     * filter and classifier.
     *
     * @param jsonConfiguration
     *            the JSON configuration specifying lower and upper limits
     * @param key
     *            the unique key of this {@code Rule}, or {@code null} to generate one
     * @param description
     *            the {@code Rule} description
     * @param groovyFilter
     *            a (possibly {@code null}) Groovy expression to be used as a {@code Position}
     *            filter
     * @param groupClassifier
     *            the (possibly {@code null}) {@code EvaluationGroupClassifier} to use, which may
     *            also implement {@code GroupAggregator}
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
                (groovyFilter == null ? null : GroovyPositionFilter.of(groovyFilter)),
                configuration.lowerLimit, configuration.upperLimit, groupClassifier);
    }

    /**
     * Creates a new {@code ConcentrationRule} with the given key, description, filter, lower and
     * upper limit.
     *
     * @param key
     *            the unique key of this {@code Rule}, or {@code null} to generate one
     * @param description
     *            the {@code Rule} description
     * @param positionFilter
     *            the filter to be applied to {@code Position}s to determine concentration
     * @param lowerLimit
     *            the lower limit of acceptable concentration values
     * @param upperLimit
     *            the upper limit of acceptable concentration values
     * @param groupClassifier
     *            the (possibly {@code null}) {@code EvaluationGroupClassifier} to use, which may
     *            also implement {@code GroupAggregator}
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
        // total amount of the Portfolio. (Eventually this could support scaling by other aggregate
        // Portfolio attributes.) Note that this requires that the specified PositionSupplier must
        // be a Portfolio itself, or the evaluation context must provide the total market value.

        TotalMarketValuePositionCalculator calculator = new TotalMarketValuePositionCalculator();

        double subtotalAmount =
                calculator.calculate(positions, getPositionFilter(), evaluationContext);
        double totalAmount;
        if (positions instanceof Portfolio) {
            // use the Portfolio itself to obtain the market value
            totalAmount = positions.getTotalMarketValue();
        } else {
            // expect the market value to be set in the context
            totalAmount = evaluationContext.getPortfolioMarketValue();
        }

        return subtotalAmount / totalAmount;
    }
}
