package org.slaq.slaqworx.panoptes.trade;

/**
 * The interface for a service that provides access to {@link Trade} data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface TradeProvider {
  /**
   * Obtains the {@link Trade} corresponding to the given ID.
   *
   * @param key
   *     the key identifying the {@link Trade} to be obtained
   *
   * @return the {@link Trade} corresponding to the given key, or {@code null} if it could not be
   *     located
   */
  public Trade getTrade(TradeKey key);
}
