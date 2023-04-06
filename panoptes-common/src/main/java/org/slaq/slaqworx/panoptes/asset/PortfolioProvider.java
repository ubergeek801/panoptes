package org.slaq.slaqworx.panoptes.asset;

import javax.annotation.Nonnull;

/**
 * The interface for a service that provides access to {@link Portfolio} data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface PortfolioProvider {
  /**
   * Obtains the {@link Portfolio} corresponding to the given key.
   *
   * @param key the key identifying the {@link Portfolio} to be obtained
   * @return the {@link Portfolio} corresponding to the given key, or {@code null} if it could not
   *     be located
   */
  Portfolio getPortfolio(@Nonnull PortfolioKey key);
}
