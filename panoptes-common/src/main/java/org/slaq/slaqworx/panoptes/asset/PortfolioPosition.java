package org.slaq.slaqworx.panoptes.asset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.trade.TaxLot;

/**
 * A {@link Position} that is held by a {@link Portfolio} and, as such, is an aggregate of {@link
 * TaxLot}s.
 *
 * @author jeremy
 */
public class PortfolioPosition extends AbstractPosition {
  @Nonnull private final ArrayList<TaxLot> taxLots;
  private final double amount;

  /**
   * Creates a new {@link PortfolioPosition} aggregating the given {@link TaxLot}s.
   *
   * @param taxLots a {@link Collection} of one or more {@link TaxLot}s to be aggregated by this
   *     {@link Position}
   */
  public PortfolioPosition(@Nonnull Collection<TaxLot> taxLots) {
    this.taxLots = new ArrayList<>(taxLots);
    amount = this.taxLots.stream().mapToDouble(TaxLot::getAmount).sum();
  }

  @Override
  public double getAmount() {
    return amount;
  }

  @Override
  @Nonnull
  public SecurityKey getSecurityKey() {
    // all TaxLots must be on the same Security, so just take the first
    return taxLots.get(0).getSecurityKey();
  }

  @Override
  @Nonnull
  public Stream<? extends Position> getTaxLots() {
    return taxLots.stream();
  }
}
