package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * A {@code PortfolioEvent} which encapsulates a command to be executed against a portfolio, such as
 * to evaluate trade compliance.
 *
 * @author jeremy
 */
public class PortfolioCommandEvent extends PortfolioEvent {
    private final long eventId;
    private final PortfolioKey portfolioKey;

    /**
     * Creates a new {@code PortfolioCommandEvent}.
     *
     * @param eventId
     *            an ID identifying the event
     * @param portfolioKey
     *            a {@code PortfolioKey} identifying the associated portfolio
     */
    public PortfolioCommandEvent(long eventId, PortfolioKey portfolioKey) {
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
    public PortfolioKey getKey() {
        return portfolioKey;
    }
}
