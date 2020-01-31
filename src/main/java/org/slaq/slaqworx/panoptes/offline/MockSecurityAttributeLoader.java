package org.slaq.slaqworx.panoptes.offline;

import javax.inject.Singleton;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;

import org.slaq.slaqworx.panoptes.data.SecurityAttributeLoader;

/**
 * {@code MockSecurityAttributeLoader} is a {@code SecurityAttributeLoader} that does nothing
 * (leaving the default {@code SecurityAttribute}s in place).
 *
 * @author jeremy
 */
@Singleton
@Primary
@Requires(env = { "offline", "test" })
public class MockSecurityAttributeLoader implements SecurityAttributeLoader {
    /**
     * Creates a new {@code MockSecurityAttributeLoader}. Restricted because this class should be
     * obtained through the {@code ApplicationContext}.
     */
    protected MockSecurityAttributeLoader() {
        // nothing to do
    }

    @Override
    public void loadSecurityAttributes() {
        // nothing to do
    }
}
