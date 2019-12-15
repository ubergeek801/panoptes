package org.slaq.slaqworx.panoptes.service;

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

    /**
     * Creates a new {@code PortfolioService}.
     *
     * @param assetCache
     *            the {@code AssetCache} to be used to obtain cached data
     */
    protected PortfolioService(AssetCache assetCache) {
        this.assetCache = assetCache;
    }

    /**
     * Obtains information about the specified {@code Portfolio}.
     *
     * @param id
     *            the ID of the {@code Portfolio} on which to obtain information
     * @param version
     *            the version of the {@code Portfolio} on which to obtain information
     * @return the requested {@code Portfolio}, or {@code null} if it does not exist
     */
    @Get("/key/{id}")
    public Portfolio getPortfolio(String id, int version) {
        return assetCache.getPortfolio(new PortfolioKey(id, version));
    }
}
