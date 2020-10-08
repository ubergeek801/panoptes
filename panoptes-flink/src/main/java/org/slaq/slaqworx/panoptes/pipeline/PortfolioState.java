package org.slaq.slaqworx.panoptes.pipeline;

import org.slaq.slaqworx.panoptes.asset.Portfolio;

public class PortfolioState {
    private final Portfolio portfolio;
    private boolean isPublished;

    public PortfolioState(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean isPublished) {
        this.isPublished = isPublished;
    }
}
