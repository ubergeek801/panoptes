package org.slaq.slaqworx.panoptes.event;

import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * A {@link PortfolioEvent} which supplies portfolio data.
 *
 * @param portfolio the {@link Portfolio} associated with this event
 * @author jeremy
 */
public record PortfolioDataEvent(@Nonnull Portfolio portfolio) implements PortfolioEvent {
  /** Creates a new {@link PortfolioDataEvent}. */
  public PortfolioDataEvent(@Nonnull Portfolio portfolio) {
    this.portfolio = portfolio;
  }

  @Override
  public PortfolioKey getBenchmarkKey() {
    return portfolio.getBenchmarkKey();
  }

  @Override
  @Nonnull
  public PortfolioKey getKey() {
    return portfolio.getKey();
  }
}
