package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;

import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A Position is a holding of some amount of a particular Security by some Portfolio.
 *
 * @author jeremy
 */
public interface Position extends Keyed<PositionKey>, Serializable {
    /**
     * Obtains the amount of this Position.
     *
     * @return the amount
     */
    public double getAmount();

    @Override
    public PositionKey getKey();

    /**
     * Obtains the Security held by this Position.
     *
     * @param SecurityProvider
     *            the SecurityProvider from which to obtain the Security
     * @return the Security held by this Position
     */
    public Security getSecurity(SecurityProvider securityProvider);

    /**
     * Obtains the key of the Security held by this Position.
     *
     * @return the key of the Security held by this Position
     */
    public SecurityKey getSecurityKey();
}
