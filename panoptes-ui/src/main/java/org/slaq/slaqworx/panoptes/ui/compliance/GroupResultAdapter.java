package org.slaq.slaqworx.panoptes.ui.compliance;

import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;

/**
 * Adapts evaluation group-level results to a tabular representation. Its parent is typically a
 * {@link PortfolioRuleResultAdapter}.
 *
 * @param parent
 *     the parent row of this row
 * @param groupResult
 *     the {@link EvaluationGroup} and its corresponding {@link ValueResult} to be adapted to row
 *     format
 *
 * @author jeremy
 */
public record GroupResultAdapter(PortfolioRuleResultAdapter parent,
                                 Entry<EvaluationGroup, ValueResult> groupResult)
    implements EvaluationResultRow {
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
  @Nonnull
  public Stream<EvaluationResultRow> getChildren() {
    return Stream.empty();
  }

  @Override
  public String getGroup() {
    return groupResult.getKey().id();
  }

  @Override
  @Nonnull
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
  public boolean isPassed() {
    return groupResult.getValue().isPassed();
  }
}
