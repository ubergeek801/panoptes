package org.slaq.slaqworx.panoptes.data;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * PortfolioRepository is a CrudRepository used to access Portfolio data.
 *
 * @author jeremy
 */
public interface PortfolioRepository
        extends CrudRepositoryWithAllIdsQuery<Portfolio, PortfolioKey> {
    // trivial extension
}
