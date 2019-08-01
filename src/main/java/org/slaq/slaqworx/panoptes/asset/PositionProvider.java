package org.slaq.slaqworx.panoptes.asset;

/**
 * PositionProvider is the interface for a service that provides access to Position data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface PositionProvider {
    /**
     * Obtains the Position corresponding to the given key.
     *
     * @param key
     *            the key identifying the Position to be obtained
     * @return the Position corresponding to the given key, or null if it could not be located
     */
    public Position getPosition(PositionKey key);
}
