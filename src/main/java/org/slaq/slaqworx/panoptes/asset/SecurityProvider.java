package org.slaq.slaqworx.panoptes.asset;

/**
 * {@code SecurityProvider} is the interface for a service that provides access to {@code Security}
 * data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface SecurityProvider {
    /**
     * Obtains the {@code Security} corresponding to the given ID.
     *
     * @param key
     *            the key identifying the {@code Security} to be obtained
     * @return the {@code Security} corresponding to the given key, or {@code null} if it could not
     *         be located
     */
    public Security getSecurity(SecurityKey key);
}
