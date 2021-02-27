package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * A {@code PortfolioEvent} which indicates a change to a security broadcast to each portfolio.
 *
 * @author jeremy
 */
public class SecurityUpdateEvent implements PortfolioEvent {
  private final PortfolioKey portfolioKey;
  private final SecurityKey securityKey;

  /**
   * Creates a new {@code SecurityUpdateEvent}.
   *
   * @param portfolioKey
   *     the portfolio which is the target of this event
   * @param securityKey
   *     a key identifying the {@code Security} that was changed
   */
  public SecurityUpdateEvent(PortfolioKey portfolioKey, SecurityKey securityKey) {
    this.portfolioKey = portfolioKey;
    this.securityKey = securityKey;
  }

  @Override
  public PortfolioKey getBenchmarkKey() {
    return null;
  }

  @Override
  public PortfolioKey getKey() {
    return portfolioKey;
  }

  /**
   * Obtains a key identifying the changed {@code Security}.
   *
   * @return a {@code SecurityKey} identifying the security that was changed
   */
  public SecurityKey getSecurityKey() {
    return securityKey;
  }
}
