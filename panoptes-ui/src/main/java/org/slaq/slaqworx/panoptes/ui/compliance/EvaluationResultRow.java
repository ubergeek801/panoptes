package org.slaq.slaqworx.panoptes.ui.compliance;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;

/**
 * An interface that facilitates adaptation of {@link EvaluationResult}s into a tabular (or
 * hierarchical/tabular hybrid) projection. Note that any data accessor method may return {@code
 * null} if the value is not appropriate for the current hierarchy level, in addition to the usual
 * reasons.
 *
 * @author jeremy
 */
public interface EvaluationResultRow {
  /**
   * Obtains the value associated with this row's result, as calculated for the benchmark, if any.
   *
   * @return the value associated with this row's result (if value-based), or {@code null} if it
   *     does not exist (Boolean-based or value-based with no benchmark specified)
   */
  Double getBenchmarkValue();

  /**
   * Obtains the number of children of this row.
   *
   * @return this row's number of children
   */
  int getChildCount();

  /**
   * Obtains this row's children as a {@link Stream}.
   *
   * @return a {@link Stream} of this row's children
   */
  @Nonnull
  Stream<EvaluationResultRow> getChildren();

  /**
   * Obtains the {@link EvaluationGroup} corresponding to this row's result, in {@link String} form.
   *
   * @return a {@link String} representation of this row's {@link EvaluationGroup}, or {@code null}
   *     if not applicable
   */
  String getGroup();

  /**
   * Obtains a (more or less) human-readable description of the {@link Rule} that produced this
   * row's result.
   *
   * @return the {@link Rule} description
   */
  @Nonnull
  String getRuleDescription();

  /**
   * Obtains the {@link Threshold} associated with this row's result.
   *
   * @return the {@link Threshold} applied by the {@link Rule} that generated this row's result, or
   *     {@code null} if not applicable
   */
  Threshold getThreshold();

  /**
   * Obtains the value associated with this row's result.
   *
   * @return the value associated with this row's result (if value-based), or {@code null} if it
   *     does not exist (Boolean-based)
   */
  Double getValue();

  /**
   * Indicates whether the {@link Rule} evaluation associated with this row passed.
   *
   * @return {@code true} if the evaluation passed, {@code false} otherwise
   */
  boolean isPassed();
}
