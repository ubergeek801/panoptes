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
 * Note that the {@code Position}s within a {@code PositionSet} should generally be unique, but for
 * performance reasons, this is not enforced by {@code PositionSet}. While this class does not
 * depend on uniqueness, any calculations based on the {@code Position}s may be skewed if duplicate
 * {@code Position}s are present.
 *
 * @author jeremy
 */
public class PositionSet implements HierarchicalPositionSupplier {
    // even though we assume Set semantics, keeping positions in contiguous memory improves
    // calculation performance by 20%
    private final ArrayList<Position> positions;
    private final PortfolioKey portfolioKey;
    private Double totalMarketValue;

    /**
     * Creates a new {@code PositionSet} consisting of the given {@code Position}s, with no parent
     * {@code Portfolio}.
     *
     * @param positions
     *            the {@code Position}s that will comprise this {@code PositionSet}
     */
    public PositionSet(Collection<? extends Position> positions) {
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
    public PositionSet(Collection<? extends Position> positions, PortfolioKey portfolioKey) {
        this.positions = new ArrayList<>(positions);
        this.portfolioKey = portfolioKey;
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
    public PositionSet(Stream<? extends Position> positions, PortfolioKey portfolioKey) {
        this(positions.collect(Collectors.toList()), portfolioKey);
    }

    @Override
    public PortfolioKey getPortfolioKey() {
        return portfolioKey;
    }

    @Override
    public Stream<Position> getPositions() {
        return getPositions(EnumSet.noneOf(PositionHierarchyOption.class));
    }

    @Override
    public Stream<Position>
            getPositions(EnumSet<PositionHierarchyOption> positionHierarchyOptions) {
        Stream<Position> positionStream = positions.stream();

        if (positionHierarchyOptions.contains(PositionHierarchyOption.LOOKTHROUGH)) {
            // FIXME implement lookthrough hierarchy
            positionStream = positionStream;
        }

        if (positionHierarchyOptions.contains(PositionHierarchyOption.TAXLOT)) {
            // FIXME implement taxlot hierarchy
            positionStream = positionStream;
        }

        return positionStream;
    }

    @Override
    public double getTotalMarketValue() {
        if (totalMarketValue == null) {
            totalMarketValue = new TotalMarketValuePositionCalculator()
                    .calculate(getPositionsWithContext(new EvaluationContext()));
        }

        return totalMarketValue;
    }

    @Override
    public int size() {
        return positions.size();
    }
}
