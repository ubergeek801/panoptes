package org.slaq.slaqworx.panoptes.asset;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A set of {@code Position}s held by some entity, which may be (for example) a customer account, a
 * hypothetical model, or something more abstract such as a benchmark.
 *
 * @author jeremy
 */
public class Portfolio
        implements Keyed<PortfolioKey>, HierarchicalPositionSupplier, ProtobufSerializable {
    private final PortfolioKey key;
    private final String name;
    private final PortfolioKey benchmarkKey;
    private Portfolio benchmark;
    private final HashSet<Rule> rules;
    private final PositionSet<Position> positionSet;

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
     *            the (possibly {@code null}) {@code Portfolio} that acts a benchmark for the
     *            {@code Portfolio}
     * @param rules
     *            the (possibly empty) {@code Collection} of {@code Rule}s associated with the
     *            {@code Portfolio}
     */
    public Portfolio(PortfolioKey id, String name, Set<Position> positions, Portfolio benchmark,
            Collection<? extends Rule> rules) {
        this(id, name, positions, (benchmark == null ? null : benchmark.getKey()), rules);
    }

    /**
     * Creates a new {@code Portfolio} with the given key, name, {@code Position}s, benchmark and
     * {@code Rule}s.
     *
     * @param key
     *            the unique {@code Portfolio} key
     * @param name
     *            the {@code Portfolio} name/description
     * @param positions
     *            the {@code Position}s comprising the {@code Portfolio}
     * @param benchmarkKey
     *            the (possibly {@code null}) {@code Portfolio} that acts a benchmark for the
     *            {@code Portfolio}
     * @param rules
     *            the (possibly empty) {@code Collection} of {@code Rule}s associated with the
     *            {@code Portfolio}
     */
    public Portfolio(PortfolioKey key, String name, Set<Position> positions,
            PortfolioKey benchmarkKey, Collection<? extends Rule> rules) {
        this.key = key;
        this.name = name;
        this.benchmarkKey = benchmarkKey;
        this.rules = (rules == null ? new HashSet<>() : new HashSet<>(rules));
        positionSet = new PositionSet<>(positions, key);
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
        if (!key.equals(other.key)) {
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

    /**
     * Obtains the {@code PortfolioKey} corresponding to this {@code Portfolio}'s benchmark, if it
     * has one.
     *
     * @return the benchmark's {@code PortfolioKey}, or {@code null} if this {@code Portfolio} has
     *         no associated benchmark
     */
    public PortfolioKey getBenchmarkKey() {
        return benchmarkKey;
    }

    @Override
    public PortfolioKey getKey() {
        return key;
    }

    @Override
    public double getMarketValue(EvaluationContext evaluationContext) {
        return evaluationContext.getMarketValue(positionSet);
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
    public PortfolioKey getPortfolioKey() {
        return key;
    }

    @Override
    public Stream<? extends Position> getPositions() {
        return positionSet.getPositions();
    }

    /**
     * Obtains the {@code Rule}s associated with this {@code Portfolio}.
     *
     * @return a {@code Stream} of {@code Rule}s
     */
    public Stream<Rule> getRules() {
        return rules.stream();
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * Indicates that this {@code Portfolio} is "abstract," such as an index.
     *
     * @return {@code true} if this {@code Portfolio} is abstract, {@code false otherwise}
     */
    public boolean isAbstract() {
        // consider a Portfolio without Rules to be abstract
        return rules.isEmpty();
    }

    @Override
    public int size() {
        return positionSet.size();
    }

    @Override
    public String toString() {
        return "Portfolio[id=\"" + key + "\"]";
    }
}
