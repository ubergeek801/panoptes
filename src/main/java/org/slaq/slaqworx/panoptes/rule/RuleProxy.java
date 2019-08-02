package org.slaq.slaqworx.panoptes.rule;

import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;

/**
 * A RuleProxy is a proxy for a concrete Rule implementation, where only the key is known at
 * creation time. Subsequent operations on the RuleProxy are delegated to the Rule resolved by the
 * specified RuleProvider.
 *
 * @author jeremy
 */
public class RuleProxy implements Rule {
    private static final long serialVersionUID = 1L;

    private final RuleKey key;
    private final transient RuleProvider ruleProvider;

    /**
     * Creates a new RuleProxy of the given key, delegating to the given Rule provider.
     *
     * @param key
     *            the key of the proxied Rule
     * @param ruleProvider
     *            the RuleProvider by which to resolve the Rule
     */
    public RuleProxy(RuleKey key, RuleProvider ruleProvider) {
        this.key = key;
        this.ruleProvider = ruleProvider;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Rule)) {
            return false;
        }
        Rule other = (Rule)obj;
        return key.equals(other.getKey());
    }

    @Override
    public EvaluationResult evaluate(PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions, EvaluationContext evaluationContext) {
        return getRule().evaluate(portfolioPositions, benchmarkPositions, evaluationContext);
    }

    @Override
    public String getDescription() {
        return getRule().getDescription();
    }

    @Override
    public Stream<GroupAggregator<?>> getGroupAggregators() {
        return getRule().getGroupAggregators();
    }

    @Override
    public EvaluationGroupClassifier getGroupClassifier() {
        return getRule().getGroupClassifier();
    }

    @Override
    public RuleKey getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * Obtains the Rule proxied by this RuleProxy.
     *
     * @return the proxied Rule
     */
    protected Rule getRule() {
        return ruleProvider.getRule(key);
    }
}
