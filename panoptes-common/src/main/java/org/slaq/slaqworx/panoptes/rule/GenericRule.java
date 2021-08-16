package org.slaq.slaqworx.panoptes.rule;

import java.util.ArrayList;
import javax.annotation.Nonnull;
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
  @Nonnull
  private final RuleKey key;
  @Nonnull
  private final String description;
  @Nonnull
  private final EvaluationGroupClassifier groupClassifier;
  @Nonnull
  private final ArrayList<GroupAggregator> groupAggregators = new ArrayList<>();

  /**
   * Creates a new {@link GenericRule} with the given key and description.
   *
   * @param key
   *     the unique key to assign to the {@link Rule}, or {@code null} to generate one
   * @param description
   *     the description of the {@link Rule}
   */
  protected GenericRule(RuleKey key, @Nonnull String description) {
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
  protected GenericRule(RuleKey key, @Nonnull String description,
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
    if (!(obj instanceof GenericRule other)) {
      return false;
    }
    return key.equals(other.getKey());
  }

  @Override
  @Nonnull
  public ValueResult evaluate(@Nonnull PositionSupplier positions, EvaluationGroup evaluationGroup,
      @Nonnull EvaluationContext evaluationContext) {
    try {
      return eval(positions,
          evaluationGroup == null ? EvaluationGroup.defaultGroup() : evaluationGroup,
          evaluationContext);
    } catch (Exception e) {
      return new ValueResult(e);
    }
  }

  @Override
  @Nonnull
  public String getDescription() {
    return description;
  }

  @Override
  @Nonnull
  public Iterable<GroupAggregator> getGroupAggregators() {
    return groupAggregators;
  }

  @Override
  @Nonnull
  public EvaluationGroupClassifier getGroupClassifier() {
    return groupClassifier;
  }

  @Override
  @Nonnull
  public RuleKey getKey() {
    return key;
  }

  @Override
  public Double lowerLimit() {
    return null;
  }

  @Override
  @Nonnull
  public String getParameterDescription() {
    return "unknown configuration";
  }

  @Override
  public Double upperLimit() {
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
  @Nonnull
  protected abstract ValueResult eval(@Nonnull PositionSupplier positions,
      @Nonnull EvaluationGroup evaluationGroup, @Nonnull EvaluationContext evaluationContext);
}
