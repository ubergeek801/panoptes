package org.slaq.slaqworx.panoptes.trade;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Impact;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the results of a {@link Trade} evaluation. For a given {@link Portfolio} and {@link
 * Rule}, impacts by the proposed {@link Trade} are recorded by {@link EvaluationGroup}.
 *
 * @author jeremy
 */
public class TradeEvaluationResult implements ProtobufSerializable {
  private static final Logger LOG = LoggerFactory.getLogger(TradeEvaluationResult.class);
  private final TradeKey tradeKey;
  private final HashMap<PortfolioRuleKey, Map<EvaluationGroup, Impact>> ruleImpactMap =
      new HashMap<>(100);
  private Impact aggregateImpact = Impact.POSITIVE;

  /**
   * Creates a new, empty {@link TradeEvaluationResult}.
   *
   * @param tradeKey
   *     a key identifying the {@link Trade} giving rise to this result
   */
  public TradeEvaluationResult(TradeKey tradeKey) {
    this.tradeKey = tradeKey;
  }

  /**
   * Records an impact corresponding to the given {@link Portfolio}, {@link Rule} and {@link
   * EvaluationGroup}.
   *
   * @param portfolioKey
   *     a key identifying the {@link Portfolio} on which the impact occurred
   * @param ruleKey
   *     a key identifying the {@link Rule} for which the impact occurred
   * @param evaluationGroup
   *     the {@link EvaluationGroup} on which the impact occurred
   * @param impact
   *     the impact that was determined during evaluation
   */
  public void addImpact(PortfolioKey portfolioKey, RuleKey ruleKey, EvaluationGroup evaluationGroup,
      Impact impact) {
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
   * Updates this {@link TradeEvaluationResult} with impacts based on the given {@link Portfolio}
   * evaluation results. Provided primarily for convenience as an accumulator for {@code
   * Stream.collect()}.
   *
   * @param portfolioResults
   *     a {@link Pair} consisting of a key identifying the {@link Portfolio} under evaluation, and
   *     a {@link Map} correlating a {@link Rule}'s key with its results
   */
  public void addImpacts(Pair<PortfolioKey, Map<RuleKey, EvaluationResult>> portfolioResults) {
    addImpacts(portfolioResults.getLeft(), portfolioResults.getRight());
  }

  /**
   * Updates this {@link TradeEvaluationResult} with impacts based on the given {@link Rule}
   * evaluation results.
   *
   * @param portfolioKey
   *     a key identifying the {@link Portfolio} under evaluation
   * @param ruleResults
   *     a {@link Map} correlating a {@link Rule}'s key with its results
   */
  public void addImpacts(PortfolioKey portfolioKey, Map<RuleKey, EvaluationResult> ruleResults) {
    ruleResults.forEach((ruleKey, groupResults) -> groupResults.getProposedResults()
        .forEach((group, proposedResult) -> {
          ValueResult portfolioResult = groupResults.getResult(group);
          addImpact(portfolioKey, ruleKey, group, proposedResult.compare(portfolioResult));
        }));
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
    TradeEvaluationResult other = (TradeEvaluationResult) obj;

    return aggregateImpact == other.aggregateImpact &&
        Objects.equals(ruleImpactMap, other.ruleImpactMap) &&
        Objects.equals(tradeKey, other.tradeKey);
  }

  /**
   * Obtains the {@link Impact}s recorded in this result.
   *
   * @return a {@link Map} associating a {@link Portfolio} and {@link Rule} with another {@link Map}
   *     associating an individual {@link EvaluationGroup} with its measured impact
   */
  public Map<PortfolioRuleKey, Map<EvaluationGroup, Impact>> getImpacts() {
    return ruleImpactMap;
  }

  /**
   * Obtains this result's key.
   *
   * @return a {@link TradeKey}
   */
  public TradeKey getTradeKey() {
    return tradeKey;
  }

  @Override
  public int hashCode() {
    return Objects.hash(tradeKey);
  }

  /**
   * Indicates whether the evaluated {@link Trade} is compliant, which means that no {@link Rule}
   * evaluations resulted in a {@code NEGATIVE} or {@code UNKNOWN} {@link Impact}.
   *
   * @return {@code true} if the evaluation results indicate {@link Trade} compliance, {@code false}
   *     otherwise
   */
  public boolean isCompliant() {
    return aggregateImpact != Impact.NEGATIVE && aggregateImpact != Impact.UNKNOWN;
  }

  /**
   * Merges the given results into this one. Provided primarily for convenience as a combiner for
   * {@code Stream.collect()}.
   *
   * @param otherResult
   *     the {@link TradeEvaluationResult} to be merged into this one
   *
   * @return the merged {@link TradeEvaluationResult}
   */
  public TradeEvaluationResult merge(TradeEvaluationResult otherResult) {
    if (!Objects.equals(tradeKey, otherResult.getTradeKey())) {
      LOG.warn("merging results for unequal TradeKeys {}, {}", tradeKey, otherResult.getTradeKey());
    }

    otherResult.getImpacts().forEach((impactKey, impact) -> impact.forEach(
        (resultKey, resultValue) -> addImpact(impactKey.portfolioKey(), impactKey.ruleKey(),
            resultKey, resultValue)));

    return this;
  }
}
