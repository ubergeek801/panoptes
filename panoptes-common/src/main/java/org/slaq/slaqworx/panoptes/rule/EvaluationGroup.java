package org.slaq.slaqworx.panoptes.rule;

import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;

/**
 * A value type used as a key when classifying rule evaluation results.
 *
 * @param id
 *     the unique ID of the {@link EvaluationGroup}
 * @param aggregationKey
 *     the aggregation key used to define this {@link EvaluationGroup}, or {@code null} for the
 *     default
 *
 * @author jeremy
 */
public record EvaluationGroup(@Nonnull String id, String aggregationKey) {
  public static final String DEFAULT_EVALUATION_GROUP_ID = "portfolio";

  private static final EvaluationGroup DEFAULT_GROUP =
      new EvaluationGroup(DEFAULT_EVALUATION_GROUP_ID, null);

  /**
   * Obtains the default ({@link Portfolio}-level) {@link EvaluationGroup}.
   *
   * @return the default {@link EvaluationGroup}
   */
  @Nonnull
  public static EvaluationGroup defaultGroup() {
    return DEFAULT_GROUP;
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
    EvaluationGroup other = (EvaluationGroup) obj;

    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  @Nonnull
  public String toString() {
    return (aggregationKey == null ? DEFAULT_EVALUATION_GROUP_ID : (aggregationKey + "=" + id));
  }
}
