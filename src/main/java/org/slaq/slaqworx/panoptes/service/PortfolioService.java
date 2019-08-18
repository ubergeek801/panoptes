package org.slaq.slaqworx.panoptes.service;

import java.util.Collection;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.PortfolioCache;

@Controller("/portfolio")
public class PortfolioService {
    private PortfolioCache portfolioCache;

    protected PortfolioService(PortfolioCache portfolioCache) {
        this.portfolioCache = portfolioCache;
    }

    @Get("list")
    public Collection<Portfolio> getAllPortfolios() {
        return portfolioCache.getPortfolioCache().values();
    }

    @Get("/key/{id}")
    public Portfolio getPortfolio(String id, int version) {
        return portfolioCache.getPortfolio(new PortfolioKey(id, version));
    }
}
