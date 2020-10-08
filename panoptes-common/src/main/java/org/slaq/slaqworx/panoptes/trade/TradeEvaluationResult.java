package org.slaq.slaqworx.panoptes.trade;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Impact;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * {@code TradeEvaluationResult} encapsulates the results of a {@code Trade} evaluation. For a given
 * {@code Portfolio} and {@code Rule}, impacts on the proposed {@code Trade} are recorded by
 * {@code EvaluationGroup}.
 *
 * @author jeremy
 */
public class TradeEvaluationResult implements ProtobufSerializable {
    private Impact aggregateImpact = Impact.POSITIVE;

    private final HashMap<PortfolioRuleKey, Map<EvaluationGroup, Impact>> ruleImpactMap =
            new HashMap<>(100);

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
     *            a key identifying the {@code Portfolio} on which the impact occurred
     * @param ruleKey
     *            a key identifying the {@code Rule} for which the impact occurred
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
     * Updates this {@code TradeEvaluationResult} with impacts based on the given {@code Portfolio}
     * evaluation results. Provided primarily for convenience as an accumulator for
     * {@code Stream.collect()}.
     *
     * @param portfolioResults
     *            a {@code Pair} consisting of a key identifying the {@code Portfolio} under
     *            evaluation, and a {@code Map} correlating a {@code Rule}'s key with its results
     */
    public void addImpacts(Pair<PortfolioKey, Map<RuleKey, EvaluationResult>> portfolioResults) {
        addImpacts(portfolioResults.getLeft(), portfolioResults.getRight());
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
                ValueResult portfolioResult = groupResults.getResult(group);
                addImpact(portfolioKey, ruleKey, group, proposedResult.compare(portfolioResult));
            });
        });
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
        TradeEvaluationResult other = (TradeEvaluationResult)obj;
        if (aggregateImpact != other.aggregateImpact) {
            return false;
        }
        if (ruleImpactMap == null) {
            if (other.ruleImpactMap != null) {
                return false;
            }
        } else if (!ruleImpactMap.equals(other.ruleImpactMap)) {
            return false;
        }

        return true;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aggregateImpact == null) ? 0 : aggregateImpact.hashCode());
        result = prime * result + ((ruleImpactMap == null) ? 0 : ruleImpactMap.hashCode());

        return result;
    }

    /**
     * Indicates whether the evaluated {@code Trade} is compliant, which means that no {@code Rule}
     * evaluations resulted in a {@code NEGATIVE} or {@code UNKNOWN} {@code Impact}.
     *
     * @return {@code true} if the evaluation results indicate {@code Trade} compliance,
     *         {@code false} otherwise
     */
    public boolean isCompliant() {
        return aggregateImpact != Impact.NEGATIVE && aggregateImpact != Impact.UNKNOWN;
    }

    /**
     * Merges the given results into this one. Provided primarily for convenience as a combiner for
     * {@code Stream.collect()}.
     *
     * @param otherResult
     *            the {@code TradeEvaluationResult} to be merged into this one
     * @return the merged {@code TradeEvaluationResult}
     */
    public TradeEvaluationResult merge(TradeEvaluationResult otherResult) {
        otherResult.getImpacts().entrySet()
                .forEach(tradeEntry -> tradeEntry.getValue().entrySet()
                        .forEach(portfolioEntry -> addImpact(tradeEntry.getKey().getPortfolioKey(),
                                tradeEntry.getKey().getRuleKey(), portfolioEntry.getKey(),
                                portfolioEntry.getValue())));

        return this;
    }
}
