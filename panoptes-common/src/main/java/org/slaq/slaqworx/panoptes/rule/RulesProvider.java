package org.slaq.slaqworx.panoptes.rule;

import java.util.stream.Stream;

/**
 * The interface for a service that provides a predetermined set of {@code Rule}s.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface RulesProvider {
  /**
   * Obtains the {@code Rules} furnished by this {@code RulesProvider}.
   *
   * @return the {@code Rules} furnished by this {@code RulesProvider}
   */
  Stream<Rule> getRules();
}
