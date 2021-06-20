package org.slaq.slaqworx.panoptes.rule;

import javax.annotation.Nonnull;

/**
 * The interface for a service that provides access to {@link Rule} data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface RuleProvider {
  /**
   * Obtains the {@link Rule} corresponding to the given key.
   *
   * @param key
   *     the key identifying the {@link Rule} to be obtained
   *
   * @return the {@link Rule} corresponding to the given key, or {@code null} if it could not be
   *     located
   */
  Rule getRule(@Nonnull RuleKey key);
}
