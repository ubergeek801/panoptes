package org.slaq.slaqworx.panoptes.test;

import java.util.HashMap;
import java.util.Set;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.EligibilityListProvider;

/**
 * An {@link EligibilityListProvider} suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestEligibilityListProvider implements EligibilityListProvider {
  private final HashMap<String, Set<String>> eligibilityListMap = new HashMap<>();

  /**
   * Creates a new {@link TestEligibilityListProvider}. Restricted because instances of this class
   * should be obtained through {@link TestUtil}.
   */
  protected TestEligibilityListProvider() {
    // nothing to do
  }

  @Override
  public Set<String> getEligibilityList(@Nonnull String name) {
    return eligibilityListMap.get(name);
  }

  /**
   * Makes the given eligibility list available through this provider.
   *
   * @param name
   *     the name of the eligibility list to provide
   * @param members
   *     the eligibility list members
   *
   * @return the provided members
   */
  public Set<String> newEligibilityList(@Nonnull String name, @Nonnull Set<String> members) {
    eligibilityListMap.put(name, members);
    return members;
  }
}
