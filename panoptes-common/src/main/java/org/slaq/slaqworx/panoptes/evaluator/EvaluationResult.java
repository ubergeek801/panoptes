package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
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
 * @param ruleKey the key indicating the {@link Rule} for which the results were produced
 * @param results a {@link Map} containing the grouped evaluation results
 * @param proposedResults a (possibly empty but never {@code null}) {@link Map} containing the
 *     grouped evaluation results of a proposed set of {@link Position}s, if requested
 * @author jeremy
 */
public record EvaluationResult(
    @Nonnull RuleKey ruleKey,
    @Nonnull Map<EvaluationGroup, ValueResult> results,
    @Nonnull Map<EvaluationGroup, ValueResult> proposedResults)
    implements Keyed<RuleKey>, ProtobufSerializable {
  /**
   * Creates a new {@link EvaluationResult} for the specified {@link Rule} using the given grouped
   * {@link ValueResult}s.
   *
   * @param ruleKey the key indicating the {@link Rule} for which the results were produced
   * @param results a {@link Map} containing the grouped evaluation results
   */
  public EvaluationResult(RuleKey ruleKey, Map<EvaluationGroup, ValueResult> results) {
    this(ruleKey, results, null);
  }

  /**
   * Creates a new {@link EvaluationResult} for the specified {@link Rule} using the given grouped
   * {@link ValueResult}s and proposed {@link ValueResult}s.
   */
  public EvaluationResult(
      RuleKey ruleKey,
      Map<EvaluationGroup, ValueResult> results,
      Map<EvaluationGroup, ValueResult> proposedResults) {
    this.ruleKey = ruleKey;
    this.results = results;
    this.proposedResults = (proposedResults == null ? Collections.emptyMap() : proposedResults);
  }

  @Override
  @Nonnull
  public RuleKey getKey() {
    return ruleKey;
  }

  /**
   * Obtains the proposed result corresponding to the specified group, if any.
   *
   * @param group the group for which to obtain proposed results
   * @return a {@link ValueResult} describing the requested proposed results, or {@code null} if
   *     there were no proposed results for the specified group
   */
  public ValueResult getProposedResult(EvaluationGroup group) {
    return proposedResults.get(group);
  }

  /**
   * Obtains the result corresponding to the specified group, if any.
   *
   * @param group the group for which to obtain results
   * @return a {@link ValueResult} describing the requested results, or {@code null} if there were
   *     no results for the specified group
   */
  public ValueResult getResult(EvaluationGroup group) {
    return results.get(group);
  }

  /**
   * Obtains the key identifying the {@link Rule} for which these results were produced.
   *
   * @return a {@link RuleKey}
   */
  @Nonnull
  public RuleKey getRuleKey() {
    return ruleKey;
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
