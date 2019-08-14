package org.slaq.slaqworx.panoptes.asset;

import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A {@code Position} is a holding of some amount of a particular {@code Security} by some
 * {@code Portfolio}.
 *
 * @author jeremy
 */
public interface Position extends Keyed<PositionKey> {
    /**
     * Obtains the amount held by this {@code Position}.
     *
     * @return the amount
     */
    public double getAmount();

    @Override
    public PositionKey getKey();

    /**
     * Obtains the {@code Security} held by this {@code Position}.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} from which to obtain the {@code Security} data
     * @return the {@code Security} held by this {@code Position}
     */
    public Security getSecurity(SecurityProvider securityProvider);

    /**
     * Obtains the key of the {@code Security} held by this {@code Position}.
     *
     * @return the key of the {@code Security} held by this {@code Position}
     */
    public SecurityKey getSecurityKey();
}
