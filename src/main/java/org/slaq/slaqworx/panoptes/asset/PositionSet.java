package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.calc.TotalAmountPositionCalculator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * A PositionSet encapsulates a set of Positions, optionally related to a parent Portfolio. If the
 * parent is specified, it should not be assumed that the members of this PositionSet are also
 * members of the Portfolio's Positions; rather, the relationship exists only so that Position
 * processing logic may access Portfolio-level data if necessary.
 * <p>
 * Note that the Positions within a PositionSet should generally be unique, but for performance
 * reasons, this is not enforced by PositionSet. While this class does not depend on uniqueness, any
 * calculations based on the Positions will be skewed if duplicate Positions are present.
 *
 * @author jeremy
 */
public class PositionSet implements PositionSupplier, Serializable {
    private static final long serialVersionUID = 1L;

    // even though we assume Set semantics, keeping positions in contiguous memory improves
    // calculation performance by 20%
    private final ArrayList<Position> positions;
    private final Portfolio portfolio;
    private final double totalAmount;

    /**
     * Creates a new PositionSet consisting of the given Positions, with no parent Portfolio.
     *
     * @param positions
     *            the Positions that will comprise this PositionSet
     */
    public PositionSet(Collection<Position> positions) {
        this(positions, null);
    }

    /**
     * Creates a new PositionSet consisting of the given Positions, with the given parent Portfolio.
     *
     * @param positions
     *            the Positions that will comprise this PositionSet
     * @param portfolio
     *            the (possibly null) Portfolio associated with this PositionSet
     */
    public PositionSet(Collection<Position> positions, Portfolio portfolio) {
        this.positions = new ArrayList<>(positions);
        this.portfolio = portfolio;
        totalAmount = new TotalAmountPositionCalculator().calculate(this,
                new EvaluationContext(null, null));
    }

    /**
     * Creates a new PositionSet consisting of the given Positions, with the given parent Portfolio.
     *
     * @param positions
     *            the Positions that will comprise this PositionSet
     * @param portfolio
     *            the (possibly null) Portfolio associated with this PositionSet
     */
    public PositionSet(Stream<Position> positions, Portfolio portfolio) {
        this(positions.collect(Collectors.toList()), portfolio);
    }

    @Override
    public Portfolio getPortfolio() {
        return portfolio;
    }

    @Override
    public Stream<Position> getPositions() {
        return positions.stream();
    }

    @Override
    public double getTotalAmount() {
        return totalAmount;
    }

    @Override
    public int size() {
        return positions.size();
    }
}
