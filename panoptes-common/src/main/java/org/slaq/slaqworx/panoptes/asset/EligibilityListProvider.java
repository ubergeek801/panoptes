package org.slaq.slaqworx.panoptes.asset;

import java.util.Set;
import javax.annotation.Nonnull;

/**
 * The interface for a service that provides access to {@link EligibilityList} data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface EligibilityListProvider {
  /**
   * Obtains the eligibility list corresponding to the given name.
   *
   * @param name the name identifying the {@link EligibilityList} to be obtained
   * @return the eligibility list corresponding to the given key, or {@code null} if it could not be
   *     located
   */
  Set<String> getEligibilityList(@Nonnull String name);
}
