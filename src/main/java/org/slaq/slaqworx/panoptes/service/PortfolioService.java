package org.slaq.slaqworx.panoptes.service;

import java.util.Collection;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;

/**
 * {@code PortfolioService} is a toy service at the moment.
 * 
 * @author jeremy
 */
@Controller("/portfolio")
public class PortfolioService {
    private AssetCache assetCache;

    protected PortfolioService(AssetCache assetCache) {
        this.assetCache = assetCache;
    }

    @Get("list")
    public Collection<Portfolio> getAllPortfolios() {
        return assetCache.getPortfolioCache().values();
    }

    @Get("/key/{id}")
    public Portfolio getPortfolio(String id, int version) {
        return assetCache.getPortfolio(new PortfolioKey(id, version));
    }
}
