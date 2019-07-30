package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A Portfolio is a set of Positions held by some entity, which may be (for example) a customer
 * account, a hypothetical model, or something more abstract such as a benchmark.
 *
 * @author jeremy
 */
@Entity
public class Portfolio implements Keyed<PortfolioKey>, PositionSupplier, Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private final PortfolioKey id;
    private final PortfolioKey benchmarkId;
    private final HashSet<Rule> rules;
    private final PositionSet positionSet;

    /**
     * Creates a new Portfolio with the given key and Positions, with no associated Benchmark or
     * Rules.
     *
     * @param id
     *            the unique Portfolio key
     * @param positions
     *            the Positions comprising the Portfolio
     */
    public Portfolio(PortfolioKey id, Set<Position> positions) {
        this(id, positions, null, Collections.emptySet());
    }

    /**
     * Creates a new Portfolio with the given key, Positions, Benchmark and Rules.
     *
     * @param id
     *            the unique Portfolio key
     * @param positions
     *            the Positions comprising the Portfolio
     * @param benchmark
     *            the (possibly null) Portfolio that acts a benchmark for the Portfolio
     * @param rules
     *            the (possibly empty) Rules associated with the Portfolio
     */
    public Portfolio(PortfolioKey id, Set<Position> positions, Portfolio benchmark,
            Set<Rule> rules) {
        this.id = id;
        benchmarkId = (benchmark == null ? null : benchmark.getId());
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
     * @param SecurityProvider
     *            the PortfolioProvider from which to obtain the benchmark Portfolio
     * @return this Portfolio's benchmark, or null if one is not associated
     */
    public Portfolio getBenchmark(PortfolioProvider portfolioProvider) {
        return (benchmarkId == null ? null : portfolioProvider.getPortfolio(benchmarkId));
    }

    @Override
    public PortfolioKey getId() {
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
        return id.hashCode();
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
