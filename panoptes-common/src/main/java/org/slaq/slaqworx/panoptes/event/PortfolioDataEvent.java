package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * A {@link PortfolioEvent} which supplies portfolio data.
 *
 * @author jeremy
 */
public class PortfolioDataEvent implements PortfolioEvent {
  private final Portfolio portfolio;

  /**
   * Creates a new {@link PortfolioDataEvent}.
   *
   * @param portfolio
   *     the {@link Portfolio} associated with this event
   */
  public PortfolioDataEvent(Portfolio portfolio) {
    this.portfolio = portfolio;
  }

  @Override
  public PortfolioKey getBenchmarkKey() {
    return portfolio.getBenchmarkKey();
  }

  @Override
  public PortfolioKey getKey() {
    return portfolio.getKey();
  }

  /**
   * Obtains the {@link Portfolio} associated with this event.
   *
   * @return a {@link Portfolio}
   */
  public Portfolio getPortfolio() {
    return portfolio;
  }
}
