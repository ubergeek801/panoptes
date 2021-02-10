package org.slaq.slaqworx.panoptes.asset;

/**
 * The interface for a service that provides access to {@code Portfolio} data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface PortfolioProvider {
    /**
     * Obtains the {@code Portfolio} corresponding to the given key.
     *
     * @param key
     *            the key identifying the {@code Portfolio} to be obtained
     * @return the {@code Portfolio} corresponding to the given key, or {@code null} if it could not
     *         be located
     */
    public Portfolio getPortfolio(PortfolioKey key);
}
