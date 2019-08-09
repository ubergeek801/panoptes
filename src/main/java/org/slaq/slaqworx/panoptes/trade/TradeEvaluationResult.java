package org.slaq.slaqworx.panoptes.trade;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult.Impact;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * TradeEvaluationResult encapsulates the results of a trade evaluation. For a given Portfolio and
 * Rule, impacts are recorded by EvaluationGroup.
 *
 * @author jeremy
 */
public class TradeEvaluationResult {
    /**
     * PortfolioRuleKey is used as a key to specify or retrieve evaluation results by Portfolio and
     * Rule.
     */
    public static class PortfolioRuleKey {
        private final PortfolioKey portfolioKey;
        private final RuleKey ruleKey;

        /**
         * Creates a new PortfolioRuleKey for the given Portfolio and Rule keys.
         *
         * @param portfolioKey
         *            the key of the referenced Portfolio
         * @param ruleKey
         *            the key of the referenced Rule
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
         * Obtains the key of the Portfolio referenced by this key.
         *
         * @return a Portfolio key
         */
        public PortfolioKey getPortfolioKey() {
            return portfolioKey;
        }

        /**
         * Obtains the key of the Rule referenced by this key.
         *
         * @return a Rule key
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

    private final ConcurrentHashMap<PortfolioRuleKey, Map<EvaluationGroup<?>, Impact>> ruleImpactMap =
            new ConcurrentHashMap<>();

    /**
     * Creates a new, empty TradeEvaluationResult.
     */
    public TradeEvaluationResult() {
        // nothing to do
    }

    /**
     * Records an impact corresponding to the given Portfolio, Rule and EvaluationGroup.
     *
     * @param portfolioKey
     *            the key identifying the Portfolio on which the impact occurred
     * @param ruleKey
     *            the key identifying the Rule for which the impact occurred
     * @param evaluationGroup
     *            the EvalautionGroup on which the impact occurred
     * @param impact
     *            the impact that was determined during evaluation
     */
    public void addImpact(PortfolioKey portfolioKey, RuleKey ruleKey,
            EvaluationGroup<?> evaluationGroup, Impact impact) {
        Map<EvaluationGroup<?>, Impact> groupImpactMap = ruleImpactMap
                .computeIfAbsent(new PortfolioRuleKey(portfolioKey, ruleKey), r -> new HashMap<>());
        groupImpactMap.put(evaluationGroup, impact);
    }

    /**
     * Obtains the impacts recorded in this result.
     *
     * @return a Map associating a Portfolio and Rule with another Map associating an individual
     *         EvaluationGroup with its measured impact
     */
    public Map<PortfolioRuleKey, Map<EvaluationGroup<?>, Impact>> getImpacts() {
        return ruleImpactMap;
    }
}
