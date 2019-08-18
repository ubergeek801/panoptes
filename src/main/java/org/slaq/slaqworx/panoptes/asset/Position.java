package org.slaq.slaqworx.panoptes.asset;

import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A {@code Position} is a holding of some amount of a particular {@code Security} by some
 * {@code Portfolio}. A {@code Position} may be durable (e.g. sourced from a database/cache) or
 * ephemeral (e.g. supplied by a proposed {@code Trade} or even a unit test).
 *
 * @author jeremy
 */
public class Position implements Keyed<PositionKey> {
    private final PositionKey key;
    private final double amount;
    private final Security security;

    /**
     * Creates a new {@code Position} with a generated key and the specified amount and
     * {@code Security}.
     *
     * @param amount
     *            the amount of the {@code Security} held in this {@code Position}
     * @param security
     *            the held {@code Security}
     */
    public Position(double amount, Security security) {
        this(null, amount, security);
    }

    /**
     * Creates a new {@code Position} with the specified ID, amount and {@code Security}.
     *
     * @param key
     *            the unique key to assign to this {@code Position}, or {@code null} to generate one
     * @param amount
     *            the amount of the {@code Security} held in this {@code Position}
     * @param securityKey
     *            the key of the held {@code Security}
     */
    public Position(PositionKey key, double amount, Security security) {
        this.key = (key == null ? new PositionKey(null) : key);
        this.amount = amount;
        this.security = security;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Position)) {
            return false;
        }
        Position other = (Position)obj;
        return key.equals(other.getKey());
    }

    /**
     * Obtains the amount held by this {@code Position}.
     *
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    @Override
    public PositionKey getKey() {
        return key;
    }

    /**
     * Obtains the {@code Security} held by this {@code Position}.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} from which to obtain the {@code Security} data
     * @return the {@code Security} held by this {@code Position}
     */
    public Security getSecurity() {
        return security;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
