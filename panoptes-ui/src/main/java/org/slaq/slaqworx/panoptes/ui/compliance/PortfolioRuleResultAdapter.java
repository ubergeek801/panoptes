package org.slaq.slaqworx.panoptes.ui.compliance;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.cache.RuleSummarizer;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleSummary;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;

/**
 * Adapts {@link Portfolio}-level results to a tabular representation. Its children are typically
 * {@link GroupResultAdapter}s.
 *
 * @author jeremy
 */
public class PortfolioRuleResultAdapter implements EvaluationResultRow {
  /** A {@link Comparator} that compares {@link EvaluationGroup}s lexically by ID. */
  private static final Comparator<? super Entry<EvaluationGroup, ValueResult>> groupComparator =
      (Comparator.comparing(e -> e.getKey().id()));

  private final Map.Entry<RuleKey, EvaluationResult> evaluationResult;
  private final AssetCache assetCache;

  /**
   * Creates a new {@link PortfolioRuleResultAdapter} adapting the given portfolio-level result and
   * using the given {@link AssetCache} to resolve cached references.
   *
   * @param evaluationResult the {@link EvaluationResult} to be adapted
   * @param assetCache the {@link AssetCache} to use to resolve cached references
   */
  public PortfolioRuleResultAdapter(
      Map.Entry<RuleKey, EvaluationResult> evaluationResult, AssetCache assetCache) {
    this.evaluationResult = evaluationResult;
    this.assetCache = assetCache;
  }

  @Override
  public Double getBenchmarkValue() {
    // not applicable at this level
    return null;
  }

  @Override
  public int getChildCount() {
    return getGroupResults().size();
  }

  @Override
  @Nonnull
  public Stream<EvaluationResultRow> getChildren() {
    return getGroupResults().entrySet().stream()
        .sorted(groupComparator)
        .map(e -> new GroupResultAdapter(this, e));
  }

  @Override
  public String getGroup() {
    // not applicable at this level
    return null;
  }

  /**
   * Obtains the {@link RuleSummary} associated with this row's evaluation.
   *
   * @return a {@link RuleSummary} summarizing the evaluated {@link Rule}
   */
  public RuleSummary getRule() {
    return assetCache.getRuleCache().executeOnKey(evaluationResult.getKey(), new RuleSummarizer());
  }

  @Override
  @Nonnull
  public String getRuleDescription() {
    return getRule().description() + " (" + getRule().parameterDescription() + ")";
  }

  /**
   * Obtains an abbreviated {@link Rule} description suitable for repeating in child rows.
   *
   * @return a short {@link Rule} description
   */
  @Nonnull
  public String getShortRuleDescription() {
    return getRule().description();
  }

  @Override
  public Threshold getThreshold() {
    // not applicable at this level
    return null;
  }

  @Override
  public Double getValue() {
    // not applicable at this level
    return null;
  }

  @Override
  public boolean isPassed() {
    return evaluationResult.getValue().isPassed();
  }

  /**
   * Obtains a {@link Map} associating each {@link EvaluationGroup} to its corresponding {@link
   * ValueResult}.
   *
   * @return a {@link Map} associating {@link EvaluationGroup} to {@link ValueResult}
   */
  @Nonnull
  protected Map<EvaluationGroup, ValueResult> getGroupResults() {
    return evaluationResult.getValue().results();
  }
}
