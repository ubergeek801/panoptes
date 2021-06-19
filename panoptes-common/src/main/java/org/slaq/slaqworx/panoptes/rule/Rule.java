package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluator;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A testable assertion against a set of {@link Position}s (typically supplied by a {@link
 * Portfolio}). A collection of {@link Rule}s is typically used to assure compliance with the
 * investment guidelines of a customer account. A {@link Rule} may be durable (e.g. sourced from a
 * database/cache) or ephemeral (e.g. supplied by a simulation mechanism or even a unit test).
 * <p>
 * In general, the calculations and results of {@link Rule} evaluation can be grouped as specified
 * by an {@link EvaluationGroupClassifier}; for example, results may be grouped by {@link Security}
 * currency by providing an {@link EvaluationGroupClassifier} which maps each currency to a distinct
 * group. The default, if an {@link EvaluationGroupClassifier} is not specified, is to calculate for
 * an entire {@link Portfolio}. Note that a {@link Rule} only specifies how its results should be
 * grouped; the actual tabulation is performed by an evaluator such as {@link PortfolioEvaluator}.
 * <p>
 * Currently, a {@link Rule} may have at most one {@link EvaluationGroupClassifier}, which may also
 * act as a {@link GroupAggregator}.
 *
 * @author jeremy
 */
public interface Rule extends BenchmarkComparable, Keyed<RuleKey> {
  /**
   * Evaluates the {@link Rule} on the given {@link Portfolio}, optionally relative to a given
   * benchmark, subject to the given evaluation context.
   * <p>
   * {@code evaluate()} wraps all {@link Exception}s (including {@link RuntimeException}s) which
   * occur during evaluation into a suitable {@link ValueResult}. Other {link Throwable}s (i.e.
   * {@link Error}s) are not caught.
   *
   * @param positions
   *     the {@link Portfolio} {@link Position}s on which to evaluate the {@link Rule}
   * @param evaluationGroup
   *     the {@link EvaluationGroup} being evaluated, or {@code null} for the default group
   * @param evaluationContext
   *     the {@link EvaluationContext} under which to evaluate
   *
   * @return the result of the {@link Rule} evaluation
   */
  @Nonnull
  public ValueResult evaluate(@Nonnull PositionSupplier positions, EvaluationGroup evaluationGroup,
      @Nonnull EvaluationContext evaluationContext);

  /**
   * Obtains the description of this {@link Rule}.
   *
   * @return the {@link Rule} description
   */
  @Nonnull
  public String getDescription();

  /**
   * Obtains this {@link Rule}'s {@link GroupAggregator}s (if any) as an {@link Iterable}.
   *
   * @return a (possibly empty) {@link Iterable} of {@link GroupAggregator}s
   */
  @Nonnull
  public Iterable<GroupAggregator> getGroupAggregators();

  /**
   * Obtain's this {@link Rule}'s (possibly {@code null}) {@link EvaluationGroupClassifier}.
   *
   * @return the {@link Rule}'s {@link EvaluationGroupClassifier}
   */
  @Nonnull
  public EvaluationGroupClassifier getGroupClassifier();

  /**
   * Obtains a description (more or less suitable for human viewing) of this {@link Rule}'s
   * parameters.
   *
   * @return a {@link String} describing this {@link Rule}'s parameters
   */
  @Nonnull
  public String getParameterDescription();

  /**
   * Obtains a {@link Predicate} which serves as a pre-evaluation {@link Position} filter. A {@code
   * null} filter (the default value) implies that all {@link Position}s should be evaluated.
   *
   * @return a {@link Predicate} to be used to filter {@link Position}s for evaluation, or {@code
   *     null} to include all {@link Position}s
   */
  public default Predicate<PositionEvaluationContext> getPositionFilter() {
    return null;
  }
}
