package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * A {@code PortfolioEvent} which supplies portfolio data.
 *
 * @author jeremy
 */
public class PortfolioDataEvent extends PortfolioEvent {
    private final Portfolio portfolio;

    /**
     * Creates a new {@code PortfolioDataEvent}.
     *
     * @param portfolio
     *            the {@code Portfolio} associated with this event
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
     * Obtains the {@code Portfolio} associated with this event.
     *
     * @return a {@code Portfolio}
     */
    public Portfolio getPortfolio() {
        return portfolio;
    }
}
