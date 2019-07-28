package org.slaq.slaqworx.panoptes.asset;

@FunctionalInterface
public interface SecurityProvider {
    /**
     * Obtains the Security corresponding to the given key.
     * 
     * @param key
     *            the key identifying the Security to be obtained
     * @return the Security corresponding to the given key, or null if it could not be located
     */
    public Security getSecurity(SecurityKey key);
}
