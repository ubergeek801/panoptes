package org.slaq.slaqworx.panoptes.rule;

import java.util.stream.Stream;

/**
 * The interface for a service that provides a predetermined set of {@link Rule}s.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface RulesProvider {
  /**
   * Obtains the {@link Rule}s furnished by this {@link RulesProvider}.
   *
   * @return the {@link Rule}s furnished by this {@link RulesProvider}
   */
  public Stream<Rule> getRules();
}
