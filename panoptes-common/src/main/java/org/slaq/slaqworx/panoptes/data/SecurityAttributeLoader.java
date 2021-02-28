package org.slaq.slaqworx.panoptes.data;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * The interface for a service that initializes the known {@link SecurityAttribute}s.
 *
 * @author jeremy
 */
public interface SecurityAttributeLoader {
  /**
   * Initializes the {@link SecurityAttribute}s from persistent data.
   */
  public void loadSecurityAttributes();
}
