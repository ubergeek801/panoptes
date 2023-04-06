package org.slaq.slaqworx.panoptes.rule;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.calc.TotalMarketValuePositionCalculator;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * A {@link LimitRule} which stipulates limits on {@link Portfolio} concentration in {@link
 * Security} holdings matched by a given {@link Position} filter, either in absolute terms or
 * relative to a benchmark. Examples of absolute rules include:
 *
 * <ul>
 *   <li>{@link Portfolio} holdings in a {@link Security} from the Emerging Markets region may not
 *       exceed 10%
 *   <li>{@link Portfolio} holdings in a US-domiciled {@link Security} must be at least 50%
 * </ul>
 *
 * Examples of benchmark-relative rules include:
 *
 * <ul>
 *   <li>{@link Portfolio} holdings in a BRL-denominated {@link Security} must be between 95% and
 *       105% of the benchmark
 *   <li>{@link Portfolio} holdings in a {@link Security} with duration < 5.0 must be less than 80%
 *       of the benchmark
 * </ul>
 *
 * @author jeremy
 */
public class ConcentrationRule extends LimitRule {
  /**
   * Creates a new {@link ConcentrationRule} with the given key, description, filter, lower and
   * upper limit.
   *
   * @param key the unique key of this {@link Rule}, or {@code null} to generate one
   * @param description the {@link Rule} description
   * @param positionFilter the filter to be applied to {@link Position}s to determine concentration
   * @param lowerLimit the lower limit of acceptable concentration values
   * @param upperLimit the upper limit of acceptable concentration values
   * @param groupClassifier the (possibly {@code null}) {@link EvaluationGroupClassifier} to use,
   *     which may also implement {@link GroupAggregator}
   */
  public ConcentrationRule(
      RuleKey key,
      @Nonnull String description,
      Predicate<PositionEvaluationContext> positionFilter,
      Double lowerLimit,
      Double upperLimit,
      EvaluationGroupClassifier groupClassifier) {
    super(key, description, positionFilter, lowerLimit, upperLimit, groupClassifier);
  }

  /**
   * Creates a new {@link ConcentrationRule} with the given JSON configuration, key, description,
   * filter and classifier.
   *
   * @param jsonConfiguration the JSON configuration specifying lower and upper limits
   * @param key the unique key of this {@link Rule}, or {@code null} to generate one
   * @param description the {@link Rule} description
   * @param groovyFilter a (possibly {@code null}) Groovy expression to be used as a {@link
   *     Position} filter
   * @param groupClassifier the (possibly {@code null}) {@link EvaluationGroupClassifier} to use,
   *     which may also implement {@link GroupAggregator}
   * @return a {@link ConcentrationRule} with the specified configuration
   */
  public static ConcentrationRule fromJson(
      @Nonnull String jsonConfiguration,
      RuleKey key,
      @Nonnull String description,
      String groovyFilter,
      EvaluationGroupClassifier groupClassifier) {
    Configuration configuration;
    try {
      configuration =
          JsonConfigurable.defaultObjectMapper().readValue(jsonConfiguration, Configuration.class);
    } catch (Exception e) {
      // TODO throw a better exception
      throw new RuntimeException("could not parse JSON configuration " + jsonConfiguration, e);
    }

    return new ConcentrationRule(
        key,
        description,
        (groovyFilter == null ? null : GroovyPositionFilter.of(groovyFilter)),
        configuration.lowerLimit,
        configuration.upperLimit,
        groupClassifier);
  }

  @Override
  public String getJsonConfiguration() {
    Configuration configuration = new Configuration();
    configuration.lowerLimit = lowerLimit();
    configuration.upperLimit = upperLimit();

    try {
      return JsonConfigurable.defaultObjectMapper().writeValueAsString(configuration);
    } catch (JsonProcessingException e) {
      // TODO throw a better exception
      throw new RuntimeException("could not serialize JSON configuration", e);
    }
  }

  @Override
  protected double getValue(
      @Nonnull PositionSupplier positions, @Nonnull EvaluationContext evaluationContext) {
    // ConcentrationRule works like a LimitRule in which the calculated value is scaled by the
    // total amount of the Portfolio. (Eventually this could support scaling by other aggregate
    // Portfolio attributes.) Note that this requires that the specified PositionSupplier must
    // be a Portfolio itself, or the evaluation context must provide the total market value.

    TotalMarketValuePositionCalculator calculator = new TotalMarketValuePositionCalculator();

    // The standard Position filter behavior is to catch a (runtime) exception and capture it in
    // the PositionEvaluationContext. In this case we consider that fatal, as we can't reliably
    // calculate the concentration, so we introduce a second filter to rethrow the exception if
    // found, otherwise carry on.
    Predicate<PositionEvaluationContext> exceptionThrowingFilter =
        (p -> {
          Throwable e = p.getException();
          if (e == null) {
            return true;
          }

          if (e instanceof RuntimeException r) {
            throw r;
          }
          // TODO wrap in a better exception
          throw new RuntimeException(e);
        });
    double subtotalAmount =
        calculator.calculate(
            positions
                .getPositionsWithContext(evaluationContext)
                .filter(getPositionFilter())
                .filter(exceptionThrowingFilter));

    return subtotalAmount / evaluationContext.getMarketValue(positions);
  }

  /**
   * Encapsulates the properties of a {@link ConcentrationRule} which are configurable via e.g.
   * JSON.
   */
  static class Configuration {
    public Double lowerLimit;
    public Double upperLimit;
  }
}
