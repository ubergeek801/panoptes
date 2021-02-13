package org.slaq.slaqworx.panoptes.test;

import java.util.HashMap;
import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;

/**
 * A {@code RuleProvider} suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestRuleProvider implements RuleProvider {
    private static final TestRuleProvider instance = new TestRuleProvider();

    /**
     * Creates and caches a {@code ConcentrationRule} with the given parameters.
     *
     * @param assetCache
     *            the {@code AssetCache} in which to cache the created {@code Rule}
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
     * @return a {@code ConcentrationRule} with the specified configuration
     */
    public static ConcentrationRule createTestConcentrationRule(AssetCache assetCache, RuleKey key,
            String description, Predicate<PositionEvaluationContext> positionFilter,
            Double lowerLimit, Double upperLimit, EvaluationGroupClassifier groupClassifier) {
        ConcentrationRule rule = new ConcentrationRule(key, description, positionFilter, lowerLimit,
                upperLimit, groupClassifier);
        assetCache.getRuleCache().set(rule.getKey(), rule);

        return rule;
    }

    /**
     * Creates and caches a {@code WeightedAverageRule} with the given parameters.
     *
     * @param assetCache
     *            the {@code AssetCache} in which to cache the created {@code Rule}
     * @param key
     *            the unique key of this {@code Rule}, or {@code null} to generate one
     * @param description
     *            the {@code Rule} description
     * @param positionFilter
     *            the filter to be applied to {@code Position}s to determine concentration
     * @param calculationAttribute
     *            the {@code SecurityAttribute} on which to calculate
     * @param lowerLimit
     *            the lower limit of acceptable concentration values
     * @param upperLimit
     *            the upper limit of acceptable concentration values
     * @param groupClassifier
     *            the (possibly {@code null}) {@code EvaluationGroupClassifier} to use, which may
     *            also implement {@code GroupAggregator}
     * @return a {@code WeightedAverageRule} with the specified configuration
     */
    public static WeightedAverageRule<Double> createTestWeightedAverageRule(AssetCache assetCache,
            RuleKey key, String description, Predicate<PositionEvaluationContext> positionFilter,
            SecurityAttribute<Double> calculationAttribute, Double lowerLimit, Double upperLimit,
            EvaluationGroupClassifier groupClassifier) {
        WeightedAverageRule<Double> rule = new WeightedAverageRule<>(key, description,
                positionFilter, calculationAttribute, lowerLimit, upperLimit, groupClassifier);
        assetCache.getRuleCache().set(rule.getKey(), rule);

        return rule;
    }

    /**
     * Obtains the singleton instance of the {@code TestRuleProvider}.
     *
     * @return a {@code TestRuleProvider}
     */
    public static TestRuleProvider getInstance() {
        return instance;
    }

    private final HashMap<RuleKey, Rule> ruleMap = new HashMap<>();

    /**
     * Creates a new {@code TestRuleProvider}. Restricted to enforce singleton semantics.
     */
    protected TestRuleProvider() {
        // nothing to do
    }

    @Override
    public Rule getRule(RuleKey key) {
        return ruleMap.get(key);
    }

    /**
     * Creates a new {@code ConcentrationRule} with the given key, description, filter, lower and
     * upper limit.
     *
     * @param key
     *            the unique key of the {@code Rule}, or {@code null} to generate one
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
     * @return a {@code Rule} with the specified configuration
     */
    public Rule newConcentrationRule(RuleKey key, String description,
            Predicate<PositionEvaluationContext> positionFilter, Double lowerLimit,
            Double upperLimit, EvaluationGroupClassifier groupClassifier) {
        Rule rule = new ConcentrationRule(key, description, positionFilter, lowerLimit, upperLimit,
                groupClassifier);
        ruleMap.put(rule.getKey(), rule);

        return rule;
    }
}
