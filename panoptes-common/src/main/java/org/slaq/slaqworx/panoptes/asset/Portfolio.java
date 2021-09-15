package org.slaq.slaqworx.panoptes.asset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RulesProvider;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A set of {@link Position}s held by some entity, which may be (for example) a customer account, a
 * hypothetical model, or something more abstract such as a benchmark.
 *
 * @author jeremy
 */
public class Portfolio
    implements Keyed<PortfolioKey>, HierarchicalPositionSupplier, ProtobufSerializable,
    RulesProvider {
  @Nonnull
  private final PortfolioKey key;
  @Nonnull
  private final String name;
  private final PortfolioKey benchmarkKey;
  @Nonnull
  private final HashSet<Rule> rules;
  @Nonnull
  private final PositionSet<? extends Position> positionSet;
  private Portfolio benchmark;

  /**
   * Creates a new {@link Portfolio} with the given key, name and {@link Position}s, with no
   * associated benchmark or {@link Rule}s.
   *
   * @param id
   *     the unique {@link Portfolio} key
   * @param name
   *     the {@link Portfolio} name/description
   * @param positions
   *     the {@link Position}s comprising the {@link Portfolio}
   */
  public Portfolio(@Nonnull PortfolioKey id, @Nonnull String name,
      @Nonnull Set<? extends Position> positions) {
    this(id, name, positions, (PortfolioKey) null, Collections.emptySet());
  }

  /**
   * Creates a new {@link Portfolio} with the given key, name, {@link Position}s, benchmark and
   * {@link Rule}s.
   *
   * @param id
   *     the unique {@link Portfolio} key
   * @param name
   *     the {@link Portfolio} name/description
   * @param positions
   *     the {@link Position}s comprising the {@link Portfolio}
   * @param benchmark
   *     the (possibly {@code null}) {@link Portfolio} that acts a benchmark for the {@link
   *     Portfolio}
   * @param rules
   *     the (possibly empty) {@link Collection} of {@link Rule}s associated with the {@link
   *     Portfolio}
   */
  public Portfolio(@Nonnull PortfolioKey id, @Nonnull String name,
      @Nonnull Set<? extends Position> positions, Portfolio benchmark,
      Collection<? extends Rule> rules) {
    this(id, name, positions, (benchmark == null ? null : benchmark.getKey()), rules);
  }

  /**
   * Creates a new {@link Portfolio} with the given key, name, {@link Position}s, benchmark and
   * {@link Rule}s.
   *
   * @param key
   *     the unique {@link Portfolio} key
   * @param name
   *     the {@link Portfolio} name/description
   * @param positions
   *     the {@link Position}s comprising the {@link Portfolio}
   * @param benchmarkKey
   *     the (possibly {@code null}) {@link Portfolio} that acts a benchmark for the {@link
   *     Portfolio}
   * @param rules
   *     the (possibly empty) {@link Collection} of {@link Rule}s associated with the {@link
   *     Portfolio}
   */
  public Portfolio(@Nonnull PortfolioKey key, @Nonnull String name,
      @Nonnull Set<? extends Position> positions, PortfolioKey benchmarkKey,
      Collection<? extends Rule> rules) {
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
    Portfolio other = (Portfolio) obj;
    return key.equals(other.key);
  }

  /**
   * Obtains this {@link Portfolio}'s benchmark, if any.
   *
   * @param portfolioProvider
   *     the {@link PortfolioProvider} from which to obtain the benchmark {@link Portfolio}
   *
   * @return this {@link Portfolio}'s benchmark, or {@code null} if one is not associated
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
   * Obtains the {@link PortfolioKey} corresponding to this {@link Portfolio}'s benchmark, if it has
   * one.
   *
   * @return the benchmark's {@link PortfolioKey}, or {@code null} if this {@link Portfolio} has no
   *     associated benchmark
   */
  public PortfolioKey getBenchmarkKey() {
    return benchmarkKey;
  }

  @Override
  @Nonnull
  public PortfolioKey getKey() {
    return key;
  }

  @Override
  public double getMarketValue(@Nonnull EvaluationContext evaluationContext) {
    return evaluationContext.getMarketValue(positionSet);
  }

  /**
   * Obtains the name/description of this {@link Portfolio}.
   *
   * @return the {@link Portfolio} name
   */
  @Nonnull
  public String getName() {
    return name;
  }

  @Override
  @JsonIgnore
  @Nonnull
  public PortfolioKey getPortfolioKey() {
    return key;
  }

  @Override
  @Nonnull
  public Stream<? extends Position> getPositions() {
    return positionSet.getPositions();
  }

  @Override
  @Nonnull
  public Stream<Rule> getRules() {
    return rules.stream();
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  /**
   * Indicates that this {@link Portfolio} is "abstract," such as an index.
   *
   * @return {@code true} if this {@link Portfolio} is abstract, {@code false otherwise}
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
  @Nonnull
  public String toString() {
    return "Portfolio[id=\"" + key + "\"]";
  }
}
