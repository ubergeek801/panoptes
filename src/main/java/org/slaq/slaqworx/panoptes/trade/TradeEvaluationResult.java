package org.slaq.slaqworx.panoptes.trade;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult.Impact;
import org.slaq.slaqworx.panoptes.rule.Rule;

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
        private final Portfolio portfolio;
        private final Rule rule;

        /**
         * Creates a new PortfolioRuleKey for the given Portfolio and Rule.
         *
         * @param portfolio
         *            the referenced Portfolio
         * @param rule
         *            the referenced Rule
         */
        public PortfolioRuleKey(Portfolio portfolio, Rule rule) {
            this.portfolio = portfolio;
            this.rule = rule;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PortfolioRuleKey other = (PortfolioRuleKey)obj;
            if (portfolio == null) {
                if (other.portfolio != null)
                    return false;
            } else if (!portfolio.equals(other.portfolio))
                return false;
            if (rule == null) {
                if (other.rule != null)
                    return false;
            } else if (!rule.equals(other.rule))
                return false;
            return true;
        }

        /**
         * Obtains the Portfolio referenced by this key.
         *
         * @return a Portfolio
         */
        public Portfolio getPortfolio() {
            return portfolio;
        }

        /**
         * Obtains the Rule referenced by this key.
         *
         * @return a Rule
         */
        public Rule getRule() {
            return rule;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((portfolio == null) ? 0 : portfolio.hashCode());
            result = prime * result + ((rule == null) ? 0 : rule.hashCode());
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
     * @param portfolio
     *            the Portfolio on which the impact occurred
     * @param rule
     *            the Rule for which the impact occurred
     * @param evaluationGroup
     *            the EvalautionGroup on which the impact occurred
     * @param impact
     *            the impact that was determined during evaluation
     */
    public void addImpact(Portfolio portfolio, Rule rule, EvaluationGroup<?> evaluationGroup,
            Impact impact) {
        Map<EvaluationGroup<?>, Impact> groupImpactMap = ruleImpactMap
                .computeIfAbsent(new PortfolioRuleKey(portfolio, rule), r -> new HashMap<>());
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
