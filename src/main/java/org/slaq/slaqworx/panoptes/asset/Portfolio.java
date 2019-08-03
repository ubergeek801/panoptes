package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A Portfolio is a set of Positions held by some entity, which may be (for example) a customer
 * account, a hypothetical model, or something more abstract such as a benchmark.
 *
 * @author jeremy
 */
public class Portfolio implements Keyed<PortfolioKey>, PositionSupplier, Serializable {
    private static final long serialVersionUID = 1L;

    private final PortfolioKey id;
    private final String name;
    private final PortfolioKey benchmarkKey;
    private final HashSet<RuleKey> ruleKeys;
    private final PositionSet positionSet;

    /**
     * Creates a new Portfolio with the given key, name and Positions, with no associated Benchmark
     * or Rules.
     *
     * @param id
     *            the unique Portfolio key
     * @param name
     *            the Portfolio name/description
     * @param positions
     *            the Positions comprising the Portfolio
     */
    public Portfolio(PortfolioKey id, String name, Set<Position> positions) {
        this(id, name, positions, (PortfolioKey)null, Collections.emptySet());
    }

    /**
     * Creates a new Portfolio with the given key, name, Positions, Benchmark and Rules.
     *
     * @param id
     *            the unique Portfolio key
     * @param name
     *            the Portfolio name/description
     * @param positions
     *            the Positions comprising the Portfolio
     * @param benchmark
     *            the (possibly null) Portfolio that acts a benchmark for the Portfolio
     * @param rules
     *            the (possibly empty) Collection of Rules associated with the Portfolio
     */
    public Portfolio(PortfolioKey id, String name, Set<Position> positions, Portfolio benchmark,
            Collection<Rule> rules) {
        this(id, name, positions, (benchmark == null ? null : benchmark.getKey()), rules);
    }

    /**
     * Creates a new Portfolio with the given key, name, Positions, Benchmark and Rules.
     *
     * @param id
     *            the unique Portfolio key
     * @param name
     *            the Portfolio name/description
     * @param positions
     *            the Positions comprising the Portfolio
     * @param benchmarkKey
     *            the (possibly null) Portfolio that acts a benchmark for the Portfolio
     * @param rules
     *            the (possibly empty) Collection of Rules associated with the Portfolio
     */
    public Portfolio(PortfolioKey id, String name, Set<Position> positions,
            PortfolioKey benchmarkKey, Collection<Rule> rules) {
        this.id = id;
        this.name = name;
        this.benchmarkKey = benchmarkKey;
        ruleKeys = (rules == null ? new HashSet<>()
                : rules.stream().map(r -> r.getKey())
                        .collect(Collectors.toCollection(HashSet::new)));
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
     * Obtains this Portfolio's benchmark, if any.
     *
     * @param portfolioProvider
     *            the PortfolioProvider from which to obtain the benchmark Portfolio
     * @return this Portfolio's benchmark, or null if one is not associated
     */
    public Portfolio getBenchmark(PortfolioProvider portfolioProvider) {
        return (benchmarkKey == null ? null : portfolioProvider.getPortfolio(benchmarkKey));
    }

    public PortfolioKey getBenchmarkKey() {
        return benchmarkKey;
    }

    @Override
    public PortfolioKey getKey() {
        return id;
    }

    /**
     * Obtains the name/description of this Portfolio.
     *
     * @return the Portfolio name
     */
    public String getName() {
        return name;
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

    public Stream<RuleKey> getRuleKeys() {
        return ruleKeys.stream();
    }

    /**
     * Obtains this Portfolio's Rules as a Stream.
     *
     * @param ruleProvider
     *            the RuleProvider from which to obtain the Rules
     * @return a Stream of Rules
     */
    public Stream<Rule> getRules(RuleProvider ruleProvider) {
        return ruleKeys.stream().map(r -> ruleProvider.getRule(r));
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
