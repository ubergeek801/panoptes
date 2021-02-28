package org.slaq.slaqworx.panoptes.rule;

import java.util.ArrayList;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;

/**
 * A partial implementation of {@link Rule} which does some basic initialization and housekeeping.
 * Extending this class is recommended but optional.
 *
 * @author jeremy
 */
public abstract class GenericRule implements Rule {
  private final RuleKey key;

  private final String description;
  private final EvaluationGroupClassifier groupClassifier;
  private final ArrayList<GroupAggregator> groupAggregators = new ArrayList<>();

  /**
   * Creates a new {@link GenericRule} with the given key and description.
   *
   * @param key
   *     the unique key to assign to the {@link Rule}, or {@code null} to generate one
   * @param description
   *     the description of the {@link Rule}
   */
  protected GenericRule(RuleKey key, String description) {
    this(key, description, null);
  }

  /**
   * Creates a new {@link GenericRule} with the given key, description and evaluation group
   * classifier.
   *
   * @param key
   *     the unique key to assign to the {@link Rule}, or {@code null} to generate one
   * @param description
   *     the description of the {@link Rule}
   * @param groupClassifier
   *     the (possibly {@code null}) {@link EvaluationGroupClassifier} to use, which may also
   *     implement {@link GroupAggregator}
   */
  protected GenericRule(RuleKey key, String description,
      EvaluationGroupClassifier groupClassifier) {
    this.key = (key == null ? new RuleKey(null) : key);
    this.description = description;
    if (groupClassifier == null) {
      this.groupClassifier = EvaluationGroupClassifier.defaultClassifier();
    } else {
      this.groupClassifier = groupClassifier;
      if (groupClassifier instanceof GroupAggregator) {
        groupAggregators.add((GroupAggregator) groupClassifier);
      }
    }
  }

  /**
   * Creates a new {@link GenericRule} with a generated key and the given description.
   *
   * @param description
   *     the description of the {@link Rule}
   */
  protected GenericRule(String description) {
    this(null, description);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof GenericRule)) {
      return false;
    }
    GenericRule other = (GenericRule) obj;
    return key.equals(other.getKey());
  }

  @Override
  public ValueResult evaluate(PositionSupplier positions, EvaluationGroup evaluationGroup,
      EvaluationContext evaluationContext) {
    try {
      return eval(positions,
          evaluationGroup == null ? EvaluationGroup.defaultGroup() : evaluationGroup,
          evaluationContext);
    } catch (Exception e) {
      return new ValueResult(e);
    }
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Iterable<GroupAggregator> getGroupAggregators() {
    return groupAggregators;
  }

  @Override
  public EvaluationGroupClassifier getGroupClassifier() {
    return groupClassifier;
  }

  @Override
  public RuleKey getKey() {
    return key;
  }

  @Override
  public Double getLowerLimit() {
    return null;
  }

  @Override
  public String getParameterDescription() {
    return "unknown configuration";
  }

  @Override
  public Double getUpperLimit() {
    return null;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public boolean isBenchmarkSupported() {
    return false;
  }

  /**
   * Evaluates the {@link Rule} on the given {@link Portfolio} {@link Position}s. The public {@code
   * evaluate()} methods ultimately delegate to this one.
   *
   * @param positions
   *     the {@link Portfolio} {@link Position}s on which to evaluate the {@link Rule}
   * @param evaluationGroup
   *     the {@link EvaluationGroup} on which the {@link Rule} is being evaluated
   * @param evaluationContext
   *     the {@link EvaluationContext} under which to evaluate
   *
   * @return the result of the {@link Rule} evaluation
   */
  protected abstract ValueResult eval(PositionSupplier positions, EvaluationGroup evaluationGroup,
      EvaluationContext evaluationContext);
}
