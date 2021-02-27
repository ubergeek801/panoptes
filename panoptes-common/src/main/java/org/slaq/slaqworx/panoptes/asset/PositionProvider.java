package org.slaq.slaqworx.panoptes.asset;

/**
 * The interface for a service that provides access to {@code Position} data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface PositionProvider {
  /**
   * Obtains the {@code Position} corresponding to the given key.
   *
   * @param key
   *     the key identifying the {@code Position} to be obtained
   *
   * @return the {@code Position} corresponding to the given key, or {@code null} if it could
   * not be
   *     located
   */
  Position getPosition(PositionKey key);
}
