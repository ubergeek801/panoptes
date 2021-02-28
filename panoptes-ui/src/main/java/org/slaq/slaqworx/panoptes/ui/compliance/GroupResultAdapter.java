package org.slaq.slaqworx.panoptes.ui.compliance;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;

/**
 * Adapts evaluation group-level results to a tabular representation. Its parent is typically a
 * {@link PortfolioRuleResultAdapter}.
 *
 * @author jeremy
 */
public class GroupResultAdapter implements EvaluationResultRow {
  private final PortfolioRuleResultAdapter parent;
  private final Map.Entry<EvaluationGroup, ValueResult> groupResult;

  /**
   * Creates a new {@link GroupResultAdapter} with the given parent and mapping the given group
   * result.
   *
   * @param parent
   *     the parent row of this row
   * @param groupResult
   *     the {@link EvaluationGroup} and its corresponding {@link ValueResult} to be adapted to row
   *     format
   */
  public GroupResultAdapter(PortfolioRuleResultAdapter parent,
      Entry<EvaluationGroup, ValueResult> groupResult) {
    this.parent = parent;
    this.groupResult = groupResult;
  }

  @Override
  public Double getBenchmarkValue() {
    // FIXME re-implement the benchmark value appropriately
    return null;
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Stream<EvaluationResultRow> getChildren() {
    return Stream.empty();
  }

  @Override
  public String getGroup() {
    return groupResult.getKey().getId();
  }

  @Override
  public String getRuleDescription() {
    return parent.getShortRuleDescription();
  }

  @Override
  public Threshold getThreshold() {
    return groupResult.getValue().getThreshold();
  }

  @Override
  public Double getValue() {
    return groupResult.getValue().getValue();
  }

  @Override
  public Boolean isPassed() {
    return groupResult.getValue().isPassed();
  }
}
