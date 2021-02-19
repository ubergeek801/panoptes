package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * A {@code PortfolioEvent} which indicates a change to a security held by the target portfolio.
 *
 * @author jeremy
 */
public class HeldSecurityEvent extends PortfolioEvent {
    private final PortfolioKey portfolioKey;
    private final SecurityKey securityKey;

    /**
     * Creates a new {@code HeldSecurityEvent}.
     *
     * @param portfolioKey
     *            the portfolio which is the target of this event
     * @param securityKey
     *            a key identifying the {@code Security} that was changed
     */
    public HeldSecurityEvent(PortfolioKey portfolioKey, SecurityKey securityKey) {
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
     * @return a {@code SecurityKey} indentifying the security that was changed
     */
    public SecurityKey getSecurityKey() {
        return securityKey;
    }
}
