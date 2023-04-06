package org.slaq.slaqworx.panoptes.asset;

import javax.annotation.Nonnull;

/**
 * The interface for a service that provides access to {@link Position} data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface PositionProvider {
  /**
   * Obtains the {@link Position} corresponding to the given key.
   *
   * @param key the key identifying the {@link Position} to be obtained
   * @return the {@link Position} corresponding to the given key, or {@code null} if it could not be
   *     located
   */
  Position getPosition(@Nonnull PositionKey key);
}
