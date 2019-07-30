package org.slaq.slaqworx.panoptes.asset;

/**
 * PortfolioProvider is the interface for a service that provides access to Portfolio data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface PortfolioProvider {
    /**
     * Obtains the Portfolio corresponding to the given key.
     *
     * @param key
     *            the key identifying the Portfolio to be obtained
     * @return the Portfolio corresponding to the given key, or null if it could not be located
     */
    public Portfolio getPortfolio(PortfolioKey key);
}
