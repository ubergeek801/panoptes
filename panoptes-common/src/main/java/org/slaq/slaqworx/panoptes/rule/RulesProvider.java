package org.slaq.slaqworx.panoptes.rule;

import java.util.stream.Stream;
import javax.annotation.Nonnull;

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
  @Nonnull
  Stream<Rule> getRules();
}
