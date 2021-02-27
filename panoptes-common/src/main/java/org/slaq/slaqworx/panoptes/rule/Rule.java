package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A testable assertion against a set of {@code Position}s (typically supplied by a {@code
 * Portfolio}). A collection of {@code Rule}s is typically used to assure compliance with the
 * investment guidelines of a customer account. A {@code Rule} may be durable (e.g. sourced from a
 * database/cache) or ephemeral (e.g. supplied by a simulation mechanism or even a unit test).
 * <p>
 * In general, the calculations and results of {@code Rule} evaluation can be grouped as specified
 * by an {@code EvaluationGroupClassifier}; for example, results may be grouped by {@code Security}
 * currency by providing an {@code EvaluationGroupClassifier} which maps each currency to a distinct
 * group. The default, if an {@code EvaluationGroupClassifier} is not specified, is to calculate for
 * an entire {@code Portfolio}. Note that a {@code Rule} only specifies how its results should be
 * grouped; the actual tabulation is performed by an evaluator such as {@code PortfolioEvaluator}.
 * <p>
 * Currently, a {@code Rule} may have at most one {@code EvaluationGroupClassifier}, which may also
 * act as a {@code GroupAggregator}.
 *
 * @author jeremy
 */
public interface Rule extends BenchmarkComparable, Keyed<RuleKey> {
  /**
   * Evaluates the {@code Rule} on the given {@code Portfolio}, optionally relative to a given
   * benchmark, subject to the given evaluation context.
   * <p>
   * {@code evaluate()} wraps all {@code Exception}s (including {@code RuntimeException}s) which
   * occur during evaluation into a suitable {@code RuleResult}. Other {@code Throwable}s (i.e.
   * {@code Error}s) are not caught.
   *
   * @param positions
   *     the {@code Portfolio} {@code Position}s on which to evaluate the {@code Rule}
   * @param evaluationGroup
   *     the {@code EvaluationGroup} being evaluated, or {@code null} for the default group
   * @param evaluationContext
   *     the {@code EvaluationContext} under which to evaluate
   *
   * @return the result of the {@code Rule} evaluation
   */
  ValueResult evaluate(PositionSupplier positions, EvaluationGroup evaluationGroup,
                       EvaluationContext evaluationContext);

  /**
   * Obtains the description of this {@code Rule}.
   *
   * @return the {@code Rule} description
   */
  String getDescription();

  /**
   * Obtains this {@code Rule}'s {@code GroupAggregator}s (if any) as an {@code Iterable}.
   *
   * @return a (possibly empty) {@code Iterable} of {@code GroupAggregator}s
   */
  Iterable<GroupAggregator> getGroupAggregators();

  /**
   * Obtain's this {@code Rule}'s (possibly {@code null}) {@code EvaluationGroupClassifier}.
   *
   * @return the {@code Rule}'s {@code EvaluationGroupClassifier}
   */
  EvaluationGroupClassifier getGroupClassifier();

  /**
   * Obtains a description (more or less suitable for human viewing) of this {@code Rule}'s
   * parameters.
   *
   * @return a {@code String} describing this {@code Rule}'s parameters
   */
  String getParameterDescription();

  /**
   * Obtains a {@code Predicate} which serves as a pre-evaluation {@code Position} filter. A
   * {@code
   * null} filter (the default value) implies that all {@code Position}s should be evaluated.
   *
   * @return a {@code Predicate} to be used to filter {@code Positions} for evaluation, or {@code
   *     null} to include all {@code Position}s
   */
  default Predicate<PositionEvaluationContext> getPositionFilter() {
    return null;
  }

  /**
   * Indicates whether benchmark comparisons are appropriate for this rule.
   *
   * @return {@code true} if benchmark comparisons are supported, {@code false} otherwise
   */
  @Override
  boolean isBenchmarkSupported();
}
