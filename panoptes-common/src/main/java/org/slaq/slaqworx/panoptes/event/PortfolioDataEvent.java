package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

public class PortfolioDataEvent extends PortfolioEvent {
    private final Portfolio portfolio;

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

    public Portfolio getPortfolio() {
        return portfolio;
    }
}
