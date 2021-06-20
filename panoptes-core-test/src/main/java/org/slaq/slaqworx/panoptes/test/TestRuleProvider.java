package org.slaq.slaqworx.panoptes.test;

import java.util.HashMap;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.GroupAggregator;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;

/**
 * A {@link RuleProvider} suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestRuleProvider implements RuleProvider {
  private static final TestRuleProvider instance = new TestRuleProvider();

  private final HashMap<RuleKey, Rule> ruleMap = new HashMap<>();

  /**
   * Creates a new {@link TestRuleProvider}. Restricted to enforce singleton semantics.
   */
  protected TestRuleProvider() {
    // nothing to do
  }

  /**
   * Creates and caches a {@link ConcentrationRule} with the given parameters.
   *
   * @param assetCache
   *     the {@link AssetCache} in which to cache the created {@link Rule}
   * @param key
   *     the unique key of this {@link Rule}, or {@code null} to generate one
   * @param description
   *     the {@link Rule} description
   * @param positionFilter
   *     the filter to be applied to {@link Position}s to determine concentration
   * @param lowerLimit
   *     the lower limit of acceptable concentration values
   * @param upperLimit
   *     the upper limit of acceptable concentration values
   * @param groupClassifier
   *     the (possibly {@code null}) {@link EvaluationGroupClassifier} to use, which may also
   *     implement {@link GroupAggregator}
   *
   * @return a {@link ConcentrationRule} with the specified configuration
   */
  public static ConcentrationRule createTestConcentrationRule(AssetCache assetCache, RuleKey key,
      String description, Predicate<PositionEvaluationContext> positionFilter, Double lowerLimit,
      Double upperLimit, EvaluationGroupClassifier groupClassifier) {
    ConcentrationRule rule =
        new ConcentrationRule(key, description, positionFilter, lowerLimit, upperLimit,
            groupClassifier);
    assetCache.getRuleCache().set(rule.getKey(), rule);

    return rule;
  }

  /**
   * Creates and caches a {@link WeightedAverageRule} with the given parameters.
   *
   * @param assetCache
   *     the {@link AssetCache} in which to cache the created {@link Rule}
   * @param key
   *     the unique key of this {@link Rule}, or {@code null} to generate one
   * @param description
   *     the {@link Rule} description
   * @param positionFilter
   *     the filter to be applied to {@link Position}s to determine concentration
   * @param calculationAttribute
   *     the {@link SecurityAttribute} on which to calculate
   * @param lowerLimit
   *     the lower limit of acceptable concentration values
   * @param upperLimit
   *     the upper limit of acceptable concentration values
   * @param groupClassifier
   *     the (possibly {@code null}) {@link EvaluationGroupClassifier} to use, which may also
   *     implement {@link GroupAggregator}
   *
   * @return a {@link WeightedAverageRule} with the specified configuration
   */
  public static WeightedAverageRule<Double> createTestWeightedAverageRule(AssetCache assetCache,
      RuleKey key, String description, Predicate<PositionEvaluationContext> positionFilter,
      SecurityAttribute<Double> calculationAttribute, Double lowerLimit, Double upperLimit,
      EvaluationGroupClassifier groupClassifier) {
    WeightedAverageRule<Double> rule =
        new WeightedAverageRule<>(key, description, positionFilter, calculationAttribute,
            lowerLimit, upperLimit, groupClassifier);
    assetCache.getRuleCache().set(rule.getKey(), rule);

    return rule;
  }

  /**
   * Obtains the singleton instance of the {@link TestRuleProvider}.
   *
   * @return a {@link TestRuleProvider}
   */
  public static TestRuleProvider getInstance() {
    return instance;
  }

  @Override
  public Rule getRule(@Nonnull RuleKey key) {
    return ruleMap.get(key);
  }

  /**
   * Creates a new {@link ConcentrationRule} with the given key, description, filter, lower and
   * upper limit.
   *
   * @param key
   *     the unique key of the {@link Rule}, or {@code null} to generate one
   * @param description
   *     the {@link Rule} description
   * @param positionFilter
   *     the filter to be applied to {@link Position}s to determine concentration
   * @param lowerLimit
   *     the lower limit of acceptable concentration values
   * @param upperLimit
   *     the upper limit of acceptable concentration values
   * @param groupClassifier
   *     the (possibly {@code null}) {@link EvaluationGroupClassifier} to use, which may also
   *     implement {@link GroupAggregator}
   *
   * @return a {@link Rule} with the specified configuration
   */
  public Rule newConcentrationRule(RuleKey key, String description,
      Predicate<PositionEvaluationContext> positionFilter, Double lowerLimit, Double upperLimit,
      EvaluationGroupClassifier groupClassifier) {
    Rule rule = new ConcentrationRule(key, description, positionFilter, lowerLimit, upperLimit,
        groupClassifier);
    ruleMap.put(rule.getKey(), rule);

    return rule;
  }
}
