package org.slaq.slaqworx.panoptes.data;

/**
 * The interface for a service that initializes the known {@code SecurityAttribute}s.
 *
 * @author jeremy
 */
public interface SecurityAttributeLoader {
    /**
     * Initializes the {@code SecurityAttribute}s from persistent data.
     */
    public void loadSecurityAttributes();
}
