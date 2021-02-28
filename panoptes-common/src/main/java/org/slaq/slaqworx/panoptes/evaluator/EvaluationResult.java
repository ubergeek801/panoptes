package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * Aggregates {@link ValueResult}s for a single {@link Rule} evaluation.
 *
 * @author jeremy
 */
public class EvaluationResult implements Keyed<RuleKey>, ProtobufSerializable {
  private final RuleKey ruleKey;

  private final Map<EvaluationGroup, ValueResult> results;
  private final Map<EvaluationGroup, ValueResult> proposedResults;

  /**
   * Creates a new {@link EvaluationResult} for the specified {@link Rule} using the given grouped
   * {@link ValueResult}s.
   *
   * @param ruleKey
   *     the key indicating the {@link Rule} for which the results were produced
   * @param results
   *     a {@link Map} containing the grouped evaluation results
   */
  public EvaluationResult(RuleKey ruleKey, Map<EvaluationGroup, ValueResult> results) {
    this(ruleKey, results, null);
  }

  /**
   * Creates a new {@link EvaluationResult} for the specified {@link Rule} using the given grouped
   * {@link ValueResult}s and proposed {@link ValueResult}s.
   *
   * @param ruleKey
   *     the key indicating the {@link Rule} for which the results were produced
   * @param results
   *     a {@link Map} containing the grouped evaluation results
   * @param proposedResults
   *     a (possibly {@code null} {@link Map} containing the grouped evaluation results of a
   *     proposed set of {@link Position}s, if requested
   */
  public EvaluationResult(RuleKey ruleKey, Map<EvaluationGroup, ValueResult> results,
      Map<EvaluationGroup, ValueResult> proposedResults) {
    this.ruleKey = ruleKey;
    this.results = results;
    this.proposedResults = (proposedResults == null ? Collections.emptyMap() : proposedResults);
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
    EvaluationResult other = (EvaluationResult) obj;

    return Objects.equals(proposedResults, other.proposedResults) &&
        Objects.equals(results, other.results) && Objects.equals(ruleKey, other.ruleKey);
  }

  @Override
  public RuleKey getKey() {
    return ruleKey;
  }

  /**
   * Obtains the proposed result corresponding to the specified group, if any.
   *
   * @param group
   *     the group for which to obtain proposed results
   *
   * @return a {@link ValueResult} describing the requested proposed results, or {@code null} if
   *     there were no proposed results for the specified group
   */
  public ValueResult getProposedResult(EvaluationGroup group) {
    return proposedResults.get(group);
  }

  /**
   * Obtains the proposed results aggregated by this {@link EvaluationResult}.
   *
   * @return a {@link Map} relating each {@link EvaluationGroup} to its corresponding proposed
   *     result
   */
  public Map<EvaluationGroup, ValueResult> getProposedResults() {
    return proposedResults;
  }

  /**
   * Obtains the result corresponding to the specified group, if any.
   *
   * @param group
   *     the group for which to obtain results
   *
   * @return a {@link ValueResult} describing the requested results, or {@code null} if there were
   *     no results for the specified group
   */
  public ValueResult getResult(EvaluationGroup group) {
    return results.get(group);
  }

  /**
   * Obtains the results aggregated by this {@link EvaluationResult}.
   *
   * @return a {@link Map} relating each {@link EvaluationGroup} to its corresponding result
   */
  public Map<EvaluationGroup, ValueResult> getResults() {
    return results;
  }

  /**
   * Obtains the key identifying the {@link Rule} for which these results were produced.
   *
   * @return a {@link RuleKey}
   */
  public RuleKey getRuleKey() {
    return ruleKey;
  }

  @Override
  public int hashCode() {
    return Objects.hash(proposedResults, results, ruleKey);
  }

  /**
   * Determines whether the aggregated results indicate a pass or failure.
   *
   * @return {@code true} if each of the individual results indicates a pass, {@code false} if at
   *     least one indicates failure
   */
  public boolean isPassed() {
    return results.values().stream().allMatch(ValueResult::isPassed);
  }

  /**
   * Obtains the number of aggregated groups/results.
   *
   * @return the number of results in this {@link EvaluationResult}
   */
  public int size() {
    return results.size();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
  }
}
