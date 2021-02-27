package org.slaq.slaqworx.panoptes.rule;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.function.Predicate;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.calc.WeightedAveragePositionCalculator;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * A {@code LimitRule} which stipulates limits on a calculated value, based on its weighted average
 * within a {@code Portfolio}'s composition, either in absolute terms or relative to a benchmark.
 * Examples of absolute rules include:
 * <ul>
 * <li>the weighted average of a {@code Portfolio}'s duration may not exceed 5.0
 * </ul>
 * Examples of benchmark-relative rules include:
 * <ul>
 * <li>the (weighted) average quality of a {@code Portfolio} must be at least 90% of the benchmark's
 * </ul>
 *
 * @param <T>
 *     the type on which the rule operates
 *
 * @author jeremy
 */
public class WeightedAverageRule<T> extends LimitRule {
  /**
   * {@code Configuration} mirrors the structure of the JSON configuration.
   */
  static class Configuration {
    public String attribute;
    public Double lowerLimit;
    public Double upperLimit;
  }

  /**
   * Creates a new {@code WeightedAverageRule} with the given JSON configuration, key,
   * description,
   * filter and classifier.
   *
   * @param jsonConfiguration
   *     the JSON configuration specifying calculation attribute, lower and upper limits
   * @param key
   *     the unique key of this rule, or {@code null} to generate one
   * @param description
   *     the rule description
   * @param groovyFilter
   *     a (possibly {@code null}) Groovy expression to be used as a {@code Position} filter
   * @param groupClassifier
   *     the (possibly {@code null}) {@code EvaluationGroupClassifier} to use, which may also
   *     implement {@code GroupAggregator}
   *
   * @return a {@code WeightedAverageRule} with the specified configuration
   */
  public static WeightedAverageRule<?> fromJson(String jsonConfiguration, RuleKey key,
                                                String description, String groovyFilter,
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

    SecurityAttribute<?> calculationAttribute = SecurityAttribute.of(configuration.attribute);

    @SuppressWarnings({"rawtypes", "unchecked"}) WeightedAverageRule<?> rule =
        new WeightedAverageRule(key, description,
            (groovyFilter == null ? null : GroovyPositionFilter.of(groovyFilter)),
            calculationAttribute, configuration.lowerLimit, configuration.upperLimit,
            groupClassifier);
    return rule;
  }

  private final SecurityAttribute<T> calculationAttribute;

  /**
   * Creates a new {@code WeightedAverageRule} with the given parameters.
   *
   * @param key
   *     the unique key of this rule, or {@code null} to generate one
   * @param description
   *     the rule description
   * @param positionFilter
   *     the (possibly {@code null}) filter to be applied to {@code Position}s
   * @param calculationAttribute
   *     the attribute on which to calculate
   * @param lowerLimit
   *     the lower limit of acceptable concentration values
   * @param upperLimit
   *     the upper limit of acceptable concentration values
   * @param groupClassifier
   *     the (possibly {@code null}) {@code EvaluationGroupClassifier} to use, which may also
   *     implement {@code GroupAggregator}
   */
  public WeightedAverageRule(RuleKey key, String description,
                             Predicate<PositionEvaluationContext> positionFilter,
                             SecurityAttribute<T> calculationAttribute, Double lowerLimit,
                             Double upperLimit,
                             EvaluationGroupClassifier groupClassifier) {
    super(key, description, positionFilter, lowerLimit, upperLimit, groupClassifier);
    this.calculationAttribute = calculationAttribute;
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
  public String getParameterDescription() {
    ArrayList<String> descriptions = new ArrayList<>();
    String superDescription = super.getParameterDescription();
    if (superDescription != null) {
      descriptions.add(superDescription);
    }
    if (calculationAttribute != null) {
      descriptions.add("attribute=\"" + calculationAttribute.getName() + "\"");
    }

    return String.join(",", descriptions);
  }

  /**
   * Obtains this {@code Rule}'s (possibly {@code null}) calculation attribute.
   *
   * @return the {@code SecurityAttribute} on which to perform calculations
   */
  protected SecurityAttribute<T> getCalculationAttribute() {
    return calculationAttribute;
  }

  @Override
  protected double getValue(PositionSupplier positions, EvaluationContext evaluationContext) {
    WeightedAveragePositionCalculator<T> calculator =
        new WeightedAveragePositionCalculator<>(calculationAttribute);

    return calculator.calculate(positions.getPositionsWithContext(evaluationContext));
  }
}
