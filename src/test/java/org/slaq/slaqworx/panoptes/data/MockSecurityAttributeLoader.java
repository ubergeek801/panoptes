package org.slaq.slaqworx.panoptes.data;

import javax.inject.Singleton;

import io.micronaut.context.annotation.Primary;

/**
 * {@code MockSecurityAttributeLoader} is a {@code SecurityAttributeLoader} that does nothing
 * (leaving the default {@code SecurityAttribute}s in place).
 *
 * @author jeremy
 */
@Singleton
@Primary
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
