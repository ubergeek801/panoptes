package org.slaq.slaqworx.panoptes;

import java.util.HashMap;
import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;

/**
 * {@code TestRuleProvider} is a {@code RuleProvider} suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestRuleProvider implements RuleProvider {
    private final HashMap<RuleKey, Rule> ruleMap = new HashMap<>();

    /**
     * Creates a new {@code TestRuleProvider}. Restricted because instances of this class should be
     * obtained through {@code TestUtil}.
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
