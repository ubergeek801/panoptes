package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.calc.TotalMarketValuePositionCalculator;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * A {@code LimitRule} which stipulates absolute limits on the market value of a {@code Portfolio}'s
 * composition. Practically speaking, there aren't many uses for this, but one important use is
 * that, combined with a filter and a zero upper limit, such a {@code Rule} can impose eligibility
 * requirements. As such, the {@code Portfolio}'s benchmark is never considered.
 *
 * @author jeremy
 */
public class MarketValueRule extends LimitRule {
    /**
     * {@code Configuration} mirrors the structure of the JSON configuration.
     */
    static class Configuration {
        public Double lowerLimit;
        public Double upperLimit;
    }

    /**
     * Creates a new {@code MarketValueRule} with the given JSON configuration, key, description,
     * filter and classifier.
     *
     * @param jsonConfiguration
     *            the JSON configuration specifying calculation attribute, lower and upper limits
     * @param key
     *            the unique key of this rule, or {@code null} to generate one
     * @param description
     *            the rule description
     * @param groovyFilter
     *            a (possibly {@code null}) Groovy expression to be used as a {@code Position}
     *            filter
     * @param groupClassifier
     *            not used; merely included to conform to expected {@code fromJson()} signature
     * @return a {@code MarketValueRule} with the specified configuration
     */
    public static MarketValueRule fromJson(String jsonConfiguration, RuleKey key,
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

        return new MarketValueRule(key, description,
                (groovyFilter == null ? null : GroovyPositionFilter.of(groovyFilter)),
                configuration.lowerLimit, configuration.upperLimit);
    }

    /**
     * Creates a new {@code MarketValueRule} with the given parameters.
     *
     * @param key
     *            the unique key of this rule, or {@code null} to generate one
     * @param description
     *            the rule description
     * @param positionFilter
     *            the (possibly {@code null}) filter to be applied to {@code Position}s
     * @param lowerLimit
     *            the lower limit of acceptable concentration values
     * @param upperLimit
     *            the upper limit of acceptable concentration values
     */
    public MarketValueRule(RuleKey key, String description,
            Predicate<PositionEvaluationContext> positionFilter, Double lowerLimit,
            Double upperLimit) {
        super(key, description, positionFilter, lowerLimit, upperLimit, null);
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
        TotalMarketValuePositionCalculator calculator = new TotalMarketValuePositionCalculator();

        return calculator.calculate(positions.getPositionsWithContext(evaluationContext));
    }
}
