package org.slaq.slaqworx.panoptes.event;

import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * A {@link PortfolioEvent} which encapsulates a command to be executed against a portfolio, such as
 * to evaluate trade compliance.
 *
 * @author jeremy
 */
public class PortfolioCommandEvent implements PortfolioEvent {
  private final long eventId;
  @Nonnull
  private final PortfolioKey portfolioKey;

  /**
   * Creates a new {@link PortfolioCommandEvent}.
   *
   * @param eventId
   *     an ID identifying the event
   * @param portfolioKey
   *     a {@link PortfolioKey} identifying the associated portfolio
   */
  public PortfolioCommandEvent(long eventId, @Nonnull PortfolioKey portfolioKey) {
    this.eventId = eventId;
    this.portfolioKey = portfolioKey;
  }

  @Override
  public PortfolioKey getBenchmarkKey() {
    return null;
  }

  public long getEventId() {
    return eventId;
  }

  @Override
  @Nonnull
  public PortfolioKey getKey() {
    return portfolioKey;
  }
}
