package org.slaq.slaqworx.panoptes.offline;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.data.SecurityAttributeLoader;

/**
 * A {@link SecurityAttributeLoader} that does nothing (leaving the default {@link
 * SecurityAttribute}s in place).
 *
 * @author jeremy
 */
@Singleton
@Primary
@Requires(env = {Environment.TEST, "offline"})
public class MockSecurityAttributeLoader implements SecurityAttributeLoader {
  /**
   * Creates a new {@link MockSecurityAttributeLoader}. Restricted because this class should be
   * obtained through the {@link ApplicationContext}.
   */
  protected MockSecurityAttributeLoader() {
    // nothing to do
  }

  @Override
  public void loadSecurityAttributes() {
    // nothing to do
  }
}
