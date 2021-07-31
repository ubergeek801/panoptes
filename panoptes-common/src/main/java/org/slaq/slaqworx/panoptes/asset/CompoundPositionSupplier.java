package org.slaq.slaqworx.panoptes.asset;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * A {@link PositionSupplier} that represents the "concatenation" of multiple {@link
 * PositionSupplier}s.
 *
 * @author jeremy
 */
public class CompoundPositionSupplier implements PositionSupplier {
  @Nonnull
  private final PositionSupplier[] suppliers;

  /**
   * Creates a new {@link CompoundPositionSupplier} concatenating the given suppliers.
   *
   * @param suppliers
   *     the suppliers to be concatenated
   */
  public CompoundPositionSupplier(@Nonnull PositionSupplier... suppliers) {
    this.suppliers = suppliers;
  }

  @Override
  public double getMarketValue(@Nonnull EvaluationContext evaluationContext) {
    return Stream.of(suppliers).mapToDouble(s -> evaluationContext.getMarketValue(s)).sum();
  }

  @Override
  @Nonnull
  public PortfolioKey getPortfolioKey() {
    // all suppliers are presumed to belong to the same Portfolio, so just return the first
    return suppliers[0].getPortfolioKey();
  }

  @Override
  @Nonnull
  public Stream<? extends Position> getPositions() {
    Stream<? extends Position> concatStream = suppliers[0].getPositions();
    for (int i = 1; i < suppliers.length; i++) {
      concatStream = Stream.concat(concatStream, suppliers[i].getPositions());
    }

    return concatStream;
  }

  @Override
  public int size() {
    return Stream.of(suppliers).mapToInt(s -> s.size()).sum();
  }
}
