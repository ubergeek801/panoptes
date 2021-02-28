package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.Portfolio;

/**
 * A value type used as a key when classifying rule evaluation results.
 *
 * @author jeremy
 */
public class EvaluationGroup {
  public static final String DEFAULT_EVALUATION_GROUP_ID = "portfolio";

  private static final EvaluationGroup DEFAULT_GROUP =
      new EvaluationGroup(DEFAULT_EVALUATION_GROUP_ID, null);
  private final String id;
  private final String aggregationKey;

  /**
   * Creates a new {@link EvaluationGroup} with the given ID and aggregation key.
   *
   * @param id
   *     the unique ID of the {@link EvaluationGroup}
   * @param aggregationKey
   *     the aggregation key used to define this {@link EvaluationGroup}, or {@code null} for the
   *     default
   */
  public EvaluationGroup(String id, String aggregationKey) {
    this.id = id;
    this.aggregationKey = aggregationKey;
  }

  /**
   * Obtains the default ({@link Portfolio}-level) {@link EvaluationGroup}.
   *
   * @return the default {@link EvaluationGroup}
   */
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

  /**
   * Obtains the aggregation key on which this {@link EvaluationGroup} is classified, or {@code
   * null} if this is the default (unclassified) group.
   *
   * @return this {@link EvaluationGroup}'s aggregation key
   */
  public String getAggregationKey() {
    return aggregationKey;
  }

  /**
   * Obtains the ID of this {@link EvaluationGroup}.
   *
   * @return the {@link EvaluationGroup} ID
   */
  public String getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return (aggregationKey == null ? DEFAULT_EVALUATION_GROUP_ID : (aggregationKey + "=" + id));
  }
}
