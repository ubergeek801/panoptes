package org.slaq.slaqworx.panoptes.asset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.calc.TotalMarketValuePositionCalculator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * A {@code PositionSet} encapsulates a set of {@code Positions}, optionally related to a parent
 * {@code Portfolio}. If the parent is specified, it should not be assumed that the members of this
 * {@code PositionSet} are also members of the {@code Portfolio}'s {@code Positions}; rather, the
 * relationship exists only so that {@code Position} processing logic may access
 * {@code Portfolio}-level data if necessary.
 * <p>
 * Because a {@code PositionSet} associated with a {@code Portfolio} may not comprise all of its
 * {@code Position}s (because the set may represent a sub-aggregation or a filtered subset), the
 * portfolio market value may be supplied at creation time, which will be used as the set's total
 * market value, rather than the calculated sum of the {@code Position}s.
 * <p>
 * Note that the {@code Position}s within a {@code PositionSet} should generally be unique, but for
 * performance reasons, this is not enforced by {@code PositionSet}. While this class does not
 * depend on uniqueness, any calculations based on the {@code Position}s may be skewed if duplicate
 * {@code Position}s are present.
 *
 * @author jeremy
 * @param <P>
 *            the concrete {@code Position} type provided by this {@code PositionSet}
 */
public class PositionSet<P extends Position> implements HierarchicalPositionSupplier {
    // even though we assume Set semantics, keeping positions in contiguous memory improves
    // calculation performance by 20%
    private final ArrayList<P> positions;
    private final PortfolioKey portfolioKey;
    private Double totalMarketValue;

    /**
     * Creates a new {@code PositionSet} consisting of the given {@code Position}s, with no parent
     * {@code Portfolio}.
     *
     * @param positions
     *            the {@code Position}s that will comprise this {@code PositionSet}
     */
    public PositionSet(Collection<P> positions) {
        this(positions, null);
    }

    /**
     * Creates a new {@code PositionSet} consisting of the given {@code Position}s, with the given
     * parent {@code Portfolio}.
     *
     * @param positions
     *            the {@code Position}s that will comprise this {@code PositionSet}
     * @param portfolioKey
     *            the (possibly {@code null}) {@code PortfolioKey} associated with this
     *            {@code PositionSet}
     */
    public PositionSet(Collection<P> positions, PortfolioKey portfolioKey) {
        this(positions, portfolioKey, null);
    }

    /**
     * Creates a new {@code PositionSet} consisting of the given {@code Position}s, with the given
     * parent {@code Portfolio} and portfolio market value.
     *
     * @param positions
     *            the {@code Position}s that will comprise this {@code PositionSet}
     * @param portfolioKey
     *            the (possibly {@code null}) {@code PortfolioKey} associated with this
     *            {@code PositionSet}
     * @param portfolioMarketValue
     *            the (possibly {@code null} portfolio market value to use
     */
    public PositionSet(Collection<P> positions, PortfolioKey portfolioKey,
            Double portfolioMarketValue) {
        this.positions = new ArrayList<>(positions);
        this.portfolioKey = portfolioKey;
        totalMarketValue = portfolioMarketValue;
    }

    /**
     * Creates a new {@code PositionSet} consisting of the given {@code Position}s, with the given
     * parent {@code Portfolio}.
     *
     * @param positions
     *            the {@code Position}s that will comprise this {@code PositionSet}
     * @param portfolioKey
     *            the (possibly {@code null}) {@code PortfolioKey} associated with this {@code
     *            PositionSet}
     */
    public PositionSet(Stream<P> positions, PortfolioKey portfolioKey) {
        this(positions.collect(Collectors.toList()), portfolioKey);
    }

    @Override
    public PortfolioKey getPortfolioKey() {
        return portfolioKey;
    }

    @Override
    public Stream<P> getPositions() {
        return positions.stream();
    }

    @Override
    public Stream<? extends Position>
            getPositions(EnumSet<PositionHierarchyOption> positionHierarchyOptions) {
        Stream<? extends Position> positionStream = positions.stream();

        if (positionHierarchyOptions.contains(PositionHierarchyOption.LOOKTHROUGH)) {
            positionStream = positionStream.flatMap(p -> p.getLookthroughPositions());
        }

        if (positionHierarchyOptions.contains(PositionHierarchyOption.TAXLOT)) {
            positionStream = positionStream.flatMap(p -> p.getTaxLots());
        }

        return positionStream;
    }

    @Override
    public double getTotalMarketValue(EvaluationContext evaluationContext) {
        if (totalMarketValue == null) {
            totalMarketValue = new TotalMarketValuePositionCalculator()
                    .calculate(getPositionsWithContext(evaluationContext));
        }

        return totalMarketValue;
    }

    @Override
    public int size() {
        return positions.size();
    }
}
