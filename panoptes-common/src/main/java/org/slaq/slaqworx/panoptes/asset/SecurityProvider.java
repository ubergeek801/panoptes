package org.slaq.slaqworx.panoptes.asset;

import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * The interface for a service that provides access to {@link Security} data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface SecurityProvider {
  /**
   * Obtains the {@link Security} corresponding to the given ID.
   *
   * @param key
   *     the key identifying the {@link Security} to be obtained
   * @param evaluationContext
   *     the {@link EvaluationContext} in which an evaluation is occurring
   *
   * @return the {@link Security} corresponding to the given key, or {@code null} if it could not be
   *     located
   */
  public Security getSecurity(SecurityKey key, EvaluationContext evaluationContext);
}
