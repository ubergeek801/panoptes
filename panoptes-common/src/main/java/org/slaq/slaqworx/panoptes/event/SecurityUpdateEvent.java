package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * A {@link PortfolioEvent} which indicates a change to a security broadcast to each portfolio.
 *
 * @author jeremy
 */
public class SecurityUpdateEvent implements PortfolioEvent {
  private final PortfolioKey portfolioKey;
  private final SecurityKey securityKey;

  /**
   * Creates a new {@link SecurityUpdateEvent}.
   *
   * @param portfolioKey
   *     the portfolio which is the target of this event
   * @param securityKey
   *     a key identifying the {@link Security} that was changed
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
   * Obtains a key identifying the changed {@link Security}.
   *
   * @return a {@link SecurityKey} identifying the security that was changed
   */
  public SecurityKey getSecurityKey() {
    return securityKey;
  }
}
