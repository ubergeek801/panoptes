package org.slaq.slaqworx.panoptes.asset;

/**
 * SecurityProvider is the interface for a service that provides access to Security data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface SecurityProvider {
    /**
     * Obtains the Security corresponding to the given ID.
     *
     * @param key
     *            the key identifying the Security to be obtained
     * @return the Security corresponding to the given key, or null if it could not be located
     */
    public Security getSecurity(SecurityKey key);
}
