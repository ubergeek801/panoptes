package org.slaq.slaqworx.panoptes.trade;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleResult;
import org.slaq.slaqworx.panoptes.rule.RuleResult.Impact;

/**
 * {@code TradeEvaluationResult} encapsulates the results of a {@code Trade} evaluation. For a given
 * {@code Portfolio} and {@code Rule}, impacts on the proposed {@code Trade} are recorded by
 * {@code EvaluationGroup}.
 *
 * @author jeremy
 */
public class TradeEvaluationResult {
    /**
     * {@code PortfolioRuleKey} is used as a key to specify or retrieve evaluation results by
     * {@code Portfolio} and {@code Rule}.
     */
    public static class PortfolioRuleKey {
        private final PortfolioKey portfolioKey;
        private final RuleKey ruleKey;

        /**
         * Creates a new {@code PortfolioRuleKey} for the given {@code Portfolio} and {@code Rule}
         * keys.
         *
         * @param portfolioKey
         *            the key of the referenced {@code Portfolio}
         * @param ruleKey
         *            the key of the referenced {@code Rule}
         */
        public PortfolioRuleKey(PortfolioKey portfolioKey, RuleKey ruleKey) {
            this.portfolioKey = portfolioKey;
            this.ruleKey = ruleKey;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            PortfolioRuleKey other = (PortfolioRuleKey)obj;
            if (portfolioKey == null) {
                if (other.portfolioKey != null) {
                    return false;
                }
            } else if (!portfolioKey.equals(other.portfolioKey)) {
                return false;
            }
            if (ruleKey == null) {
                if (other.ruleKey != null) {
                    return false;
                }
            } else if (!ruleKey.equals(other.ruleKey)) {
                return false;
            }
            return true;
        }

        /**
         * Obtains the key of the {@code Portfolio} referenced by this key.
         *
         * @return a {@code Portfolio} key
         */
        public PortfolioKey getPortfolioKey() {
            return portfolioKey;
        }

        /**
         * Obtains the key of the {@code Rule} referenced by this key.
         *
         * @return a {@code Rule} key
         */
        public RuleKey getRuleKey() {
            return ruleKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((portfolioKey == null) ? 0 : portfolioKey.hashCode());
            result = prime * result + ((ruleKey == null) ? 0 : ruleKey.hashCode());
            return result;
        }
    }

    private Impact aggregateImpact = Impact.POSITIVE;

    private final ConcurrentHashMap<PortfolioRuleKey, Map<EvaluationGroup, Impact>> ruleImpactMap =
            new ConcurrentHashMap<>();

    /**
     * Creates a new, empty {@code TradeEvaluationResult}.
     */
    public TradeEvaluationResult() {
        // nothing to do
    }

    /**
     * Records an impact corresponding to the given {@code Portfolio}, {@code Rule} and
     * {@code EvaluationGroup}.
     *
     * @param portfolioKey
     *            the key identifying the {@code Portfolio} on which the impact occurred
     * @param ruleKey
     *            the key identifying the {@code Rule} for which the impact occurred
     * @param evaluationGroup
     *            the {@code EvalautionGroup} on which the impact occurred
     * @param impact
     *            the impact that was determined during evaluation
     */
    public void addImpact(PortfolioKey portfolioKey, RuleKey ruleKey,
            EvaluationGroup evaluationGroup, Impact impact) {
        Map<EvaluationGroup, Impact> groupImpactMap = ruleImpactMap
                .computeIfAbsent(new PortfolioRuleKey(portfolioKey, ruleKey), r -> new HashMap<>());
        groupImpactMap.put(evaluationGroup, impact);

        // update the aggregate impact; it can only be downgraded
        if (impact == Impact.NEUTRAL) {
            if (aggregateImpact == Impact.POSITIVE) {
                aggregateImpact = Impact.NEUTRAL;
            }
        } else if (impact == Impact.UNKNOWN) {
            if (aggregateImpact != Impact.NEGATIVE) {
                aggregateImpact = Impact.UNKNOWN;
            }
        } else if (impact == Impact.NEGATIVE) {
            aggregateImpact = Impact.NEGATIVE;
        }
    }

    /**
     * Updates this {@code TradeEvaluationResult} with impacts based on the given {@code Rule}
     * evaluation results.
     *
     * @param portfolioKey
     *            a key identifying the {@code Portfolio} under evaluation
     * @param ruleResults
     *            a {@code Map} correlating a {@code Rule}'s key with its results
     */
    public void addImpacts(PortfolioKey portfolioKey, Map<RuleKey, EvaluationResult> ruleResults) {
        ruleResults.entrySet().forEach(ruleEntry -> {
            RuleKey ruleKey = ruleEntry.getKey();
            EvaluationResult groupResults = ruleEntry.getValue();
            groupResults.getProposedResults().forEach((group, proposedResult) -> {
                RuleResult portfolioResult = groupResults.getResult(group);
                addImpact(portfolioKey, ruleKey, group, proposedResult.compare(portfolioResult));
            });
        });
    }

    /**
     * Obtains the {@code Impact}s recorded in this result.
     *
     * @return a {@code Map} associating a {@code Portfolio} and {@code Rule} with another
     *         {@code Map} associating an individual {@code EvaluationGroup} with its measured
     *         impact
     */
    public Map<PortfolioRuleKey, Map<EvaluationGroup, Impact>> getImpacts() {
        return ruleImpactMap;
    }

    /**
     * Indicates whether the evaluated {@code Trade} is compliant, which means that no {@code Rule}
     * evaluations resulted in a {@code NEGATIVE} or {@code UNKNOWN} {@code Impact}.
     *
     * @return true if the evaluation results indicate {@code Trade} compliance, false otherwise
     */
    public boolean isCompliant() {
        return aggregateImpact != Impact.NEGATIVE && aggregateImpact != Impact.UNKNOWN;
    }
}
