package org.slaq.slaqworx.panoptes.rule;

/**
 * The interface for a service that provides access to {@code Rule} data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface RuleProvider {
  /**
   * Obtains the {@code Rule} corresponding to the given key.
   *
   * @param key
   *     the key identifying the {@code Rule} to be obtained
   *
   * @return the {@code Rule} corresponding to the given key, or {@code null} if it could not be
   *     located
   */
  Rule getRule(RuleKey key);
}
