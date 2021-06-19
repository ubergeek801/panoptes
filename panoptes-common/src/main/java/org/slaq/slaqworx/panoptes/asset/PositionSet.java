package org.slaq.slaqworx.panoptes.asset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.calc.TotalMarketValuePositionCalculator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * Encapsulates a set of {@link Position}s, optionally related to a containing {@link Portfolio}. If
 * the container is specified, it should not be assumed that the members of this {@link PositionSet}
 * are also members of the {@link Portfolio}'s {@link Position}s; rather, the relationship exists
 * only so that {@link Position} processing logic may access {@link Portfolio}-level data if
 * necessary.
 * <p>
 * Because a {@link PositionSet} associated with a {@link Portfolio} may not comprise all of its
 * {@link Position}s (because the set may represent a sub-aggregation or a filtered subset), the
 * portfolio market value may be supplied at creation time, which will be used as the set's total
 * market value, rather than the calculated sum of the {@link Position}s.
 * <p>
 * Note that the {@link Position}s within a {@link PositionSet} should generally be unique, but for
 * performance reasons, this is not enforced by {@link PositionSet}. While this class does not
 * depend on uniqueness, any calculations based on the {@link Position}s may be skewed if duplicate
 * {@link Position}s are present.
 *
 * @param <P>
 *     the concrete {@link Position} type provided by this {@link PositionSet}
 *
 * @author jeremy
 */
public class PositionSet<P extends Position> implements HierarchicalPositionSupplier {
  // even though we assume Set semantics, keeping positions in contiguous memory improves
  // calculation performance by 20%
  @Nonnull
  private final ArrayList<P> positions;
  private final PortfolioKey portfolioKey;
  private final Double totalMarketValue;

  /**
   * Creates a new {@link PositionSet} consisting of the given {@link Position}s, with no container
   * {@link Portfolio}.
   *
   * @param positions
   *     the {@link Position}s that will comprise this {@link PositionSet}
   */
  public PositionSet(@Nonnull Collection<P> positions) {
    this(positions, null);
  }

  /**
   * Creates a new {@link PositionSet} consisting of the given {@link Position}s, with the given
   * container {@link Portfolio}.
   *
   * @param positions
   *     the {@link Position}s that will comprise this {@link PositionSet}
   * @param portfolioKey
   *     the (possibly {@code null}) {@link PortfolioKey} associated with this {@link PositionSet}
   */
  public PositionSet(@Nonnull Collection<P> positions, PortfolioKey portfolioKey) {
    this(positions, portfolioKey, null);
  }

  /**
   * Creates a new {@link PositionSet} consisting of the given {@link Position}s, with the given
   * containing {@link Portfolio} and portfolio market value.
   *
   * @param positions
   *     the {@link Position}s that will comprise this {@link PositionSet}
   * @param portfolioKey
   *     the (possibly {@code null}) {@link PortfolioKey} associated with this {@link PositionSet}
   * @param portfolioMarketValue
   *     the (possibly {@code null} portfolio market value to use
   */
  public PositionSet(@Nonnull Collection<P> positions, PortfolioKey portfolioKey,
      Double portfolioMarketValue) {
    this.positions = new ArrayList<>(positions);
    this.portfolioKey = portfolioKey;
    totalMarketValue = portfolioMarketValue;
  }

  @Override
  public double getMarketValue(@Nonnull EvaluationContext evaluationContext) {
    // if a market value override is supplied, use it
    if (totalMarketValue != null) {
      return totalMarketValue;
    }

    // otherwise calculate the market value
    return new TotalMarketValuePositionCalculator()
        .calculate(getPositionsWithContext(evaluationContext));
  }

  @Override
  public PortfolioKey getPortfolioKey() {
    return portfolioKey;
  }

  @Override
  @Nonnull
  public Stream<P> getPositions() {
    return positions.stream();
  }

  @Override
  public int size() {
    return positions.size();
  }
}
