package org.slaq.slaqworx.panoptes.trade;

/**
 * The interface for a service that provides access to {@code Trade} data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface TradeProvider {
  /**
   * Obtains the {@code Trade} corresponding to the given ID.
   *
   * @param key
   *     the key identifying the {@code Trade} to be obtained
   *
   * @return the {@code Trade} corresponding to the given key, or {@code null} if it could not be
   *     located
   */
  Trade getTrade(TradeKey key);
}
