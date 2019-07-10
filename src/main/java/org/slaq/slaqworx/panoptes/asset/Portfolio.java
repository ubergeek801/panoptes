package org.slaq.slaqworx.panoptes.asset;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * A Portfolio is a set of Positions held by some entity, which may be (for example) a customer
 * account, a hypothetical model, or something more abstract such as a benchmark.
 *
 * @author jeremy
 */
public class Portfolio implements PositionSupplier {
    private final String id;
    private final Portfolio benchmark;
    private final HashSet<Rule> rules;
    private final PositionSet positionSet;

    /**
     * Creates a new Portfolio with the given ID and Positions, with no associated Benchmark or
     * Rules.
     *
     * @param id
     *            the unique Portfolio ID
     * @param positions
     *            the Positions comprising the Portfolio
     */
    public Portfolio(String id, Set<Position> positions) {
        this(id, positions, null, Collections.emptySet());
    }

    /**
     * Creates a new Portfolio with the given ID, Positions, Benchmark and Rules.
     *
     * @param id
     *            the unique Portfolio ID
     * @param positions
     *            the Positions comprising the Portfolio
     * @param benchmark
     *            the (possibly null) Portfolio that acts a benchmark for the Portfolio
     * @param rules
     *            the (possibly empty) Rules associated with the Portfolio
     */
    public Portfolio(String id, Set<Position> positions, Portfolio benchmark, Set<Rule> rules) {
        this.id = id;
        this.benchmark = benchmark;
        this.rules = new HashSet<>(rules);
        positionSet = new PositionSet(positions, this);
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
        Portfolio other = (Portfolio)obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /**
     * Obtains this Portfolio's benchmark.
     *
     * @return this Portfolio's benchmark, or null if one is not associated
     */
    public Portfolio getBenchmark() {
        return benchmark;
    }

    /**
     * Obtains this Portfolio's unique ID.
     *
     * @return this Portfolio's ID
     */
    public String getId() {
        return id;
    }

    @Override
    public Portfolio getPortfolio() {
        return this;
    }

    @Override
    public Stream<Position> getPositions() {
        return positionSet.getPositions();
    }

    /**
     * Obtains this Portfolio's Positions as a PositionSet.
     *
     * @return a PositionSet
     */
    public PositionSet getPositionSet() {
        return positionSet;
    }

    /**
     * Obtains this Portfolio's Rules as a Stream.
     *
     * @return a Stream of Rules
     */
    public Stream<Rule> getRules() {
        return rules.stream();
    }

    @Override
    public double getTotalAmount() {
        return positionSet.getTotalAmount();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public int size() {
        return positionSet.size();
    }

    @Override
    public String toString() {
        return "Portfolio[id=\"" + id + "\"]";
    }
}
