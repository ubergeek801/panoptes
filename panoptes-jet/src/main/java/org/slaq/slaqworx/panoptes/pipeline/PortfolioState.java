package org.slaq.slaqworx.panoptes.pipeline;

import org.slaq.slaqworx.panoptes.asset.Portfolio;

/**
 * A container for a portfolio and its publishing status. What "publishing" means isn't specified,
 * but would likely refer to the result of some compliance evaluation.
 *
 * @author jeremy
 */
public class PortfolioState {
  private final Portfolio portfolio;
  private boolean isPublished;

  /**
   * Creates a new {@code PortfolioState} representing the given portfolio.
   *
   * @param portfolio
   *     the {@code Portfolio} for which state is held
   */
  public PortfolioState(Portfolio portfolio) {
    this.portfolio = portfolio;
  }

  /**
   * Obtains the {@code Portfolio} for which state is held.
   *
   * @return a {@code Portfolio}
   */
  public Portfolio getPortfolio() {
    return portfolio;
  }

  /**
   * Indicates whether the {@code Portfolio} has been published.
   *
   * @return {@code true} if the portfolio has been published, {@code false} otherwise
   */
  public boolean isPublished() {
    return isPublished;
  }

  /**
   * Specifies whether the {@code Portfolio} has been published.
   *
   * @param isPublished
   *     {@code true} if the portfolio has been published, {@code false} otherwise
   */
  public void setPublished(boolean isPublished) {
    this.isPublished = isPublished;
  }
}
