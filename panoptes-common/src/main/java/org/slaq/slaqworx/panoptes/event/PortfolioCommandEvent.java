package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

public class PortfolioCommandEvent extends PortfolioEvent {
    private final long eventId;
    private final PortfolioKey portfolioKey;

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
