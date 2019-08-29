package org.slaq.slaqworx.panoptes.asset;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A {@code Portfolio} is a set of {@code Position}s held by some entity, which may be (for example)
 * a customer account, a hypothetical model, or something more abstract such as a benchmark.
 *
 * @author jeremy
 */
public class Portfolio implements Keyed<PortfolioKey>, PositionSupplier {
    private final PortfolioKey id;
    private final String name;
    private final PortfolioKey benchmarkKey;
    private Portfolio benchmark;
    private final HashSet<Rule> rules;
    private final PositionSet positionSet;

    /**
     * Creates a new {@code Portfolio} with the given key, name, {@code Position}s, benchmark and
     * {@code Rule}s.
     *
     * @param id
     *            the unique {@code Portfolio} key
     * @param name
     *            the {@code Portfolio} name/description
     * @param positions
     *            the {@code Position}s comprising the {@code Portfolio}
     * @param benchmark
     *            the (possibly null) {@code Portfolio} that acts a benchmark for the
     *            {@code Portfolio}
     * @param rules
     *            the (possibly empty) {@code Collection} of {@code Rule}s associated with the
     *            {@code Portfolio}
     */
    public Portfolio(PortfolioKey id, String name, Set<? extends Position> positions,
            Portfolio benchmark, Collection<? extends Rule> rules) {
        this(id, name, positions, (benchmark == null ? null : benchmark.getKey()), rules);
    }

    /**
     * Creates a new {@code Portfolio} with the given key, name, {@code Position}s, benchmark and
     * {@code Rule}s.
     *
     * @param id
     *            the unique {@code Portfolio} key
     * @param name
     *            the {@code Portfolio} name/description
     * @param positions
     *            the {@code Position}s comprising the {@code Portfolio}
     * @param benchmarkKey
     *            the (possibly null) {@code Portfolio} that acts a benchmark for the
     *            {@code Portfolio}
     * @param rules
     *            the (possibly empty) {@code Collection} of {@code Rule}s associated with the
     *            {@code Portfolio}
     */
    public Portfolio(PortfolioKey id, String name, Set<? extends Position> positions,
            PortfolioKey benchmarkKey, Collection<? extends Rule> rules) {
        this.id = id;
        this.name = name;
        this.benchmarkKey = benchmarkKey;
        this.rules = (rules == null ? new HashSet<>() : new HashSet<>(rules));
        positionSet = new PositionSet(positions, this);
    }

    /**
     * Creates a new {@code Portfolio} with the given key, name and {@code Position}s, with no
     * associated benchmark or {@code Rule}s.
     *
     * @param id
     *            the unique {@code Portfolio} key
     * @param name
     *            the {@code Portfolio} name/description
     * @param positions
     *            the {@code Positions} comprising the {@code Portfolio}
     */
    public Portfolio(PortfolioKey id, String name, Set<Position> positions) {
        this(id, name, positions, (PortfolioKey)null, Collections.emptySet());
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
     * Obtains this {@code Portfolio}'s benchmark, if any.
     *
     * @param portfolioProvider
     *            the {@code PortfolioProvider} from which to obtain the benchmark {@code Portfolio}
     * @return this {@code Portfolio}'s benchmark, or {@code null} if one is not associated
     */
    public Portfolio getBenchmark(PortfolioProvider portfolioProvider) {
        if (benchmarkKey == null) {
            return null;
        }

        if (benchmark == null) {
            benchmark = portfolioProvider.getPortfolio(benchmarkKey);
        }

        return benchmark;
    }

    public PortfolioKey getBenchmarkKey() {
        return benchmarkKey;
    }

    @Override
    public PortfolioKey getKey() {
        return id;
    }

    /**
     * Obtains the name/description of this {@code Portfolio}.
     *
     * @return the {@code Portfolio} name
     */
    public String getName() {
        return name;
    }

    @JsonIgnore
    @Override
    public Portfolio getPortfolio() {
        return this;
    }

    @Override
    public Stream<Position> getPositions() {
        return positionSet.getPositions();
    }

    public Stream<Rule> getRules() {
        return rules.stream();
    }

    @Override
    public double getTotalMarketValue() {
        return positionSet.getTotalMarketValue();
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
