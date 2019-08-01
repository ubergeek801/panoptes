package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;

import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A Position is a holding of some amount of a particular Security by some Portfolio.
 *
 * @author jeremy
 */
public class Position implements Keyed<PositionKey>, Serializable {
    private static final long serialVersionUID = 1L;

    private final PositionKey key;
    private final double amount;
    private final SecurityKey securityKey;

    /**
     * Creates a new Position with a generated key and the specified amount and Security.
     *
     * @param amount
     *            the amount of the Security held in this Position
     * @param securityKey
     *            the key of the held Security
     */
    public Position(double amount, SecurityKey securityKey) {
        this(null, amount, securityKey);
    }

    /**
     * Creates a new Position with the specified ID, amount and Security.
     *
     * @param key
     *            the unique key to assign to this Position, or null to generate one
     * @param amount
     *            the amount of the Security held in this Position
     * @param securityKey
     *            the key of the held Security
     */
    public Position(PositionKey key, double amount, SecurityKey securityKey) {
        this.key = (key == null ? new PositionKey(null, 1) : key);
        this.amount = amount;
        this.securityKey = securityKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Position other = (Position)obj;
        return key.equals(other.key);
    }

    /**
     * Obtains the amount of this Position.
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
     * Obtains the Security held by this Position.
     *
     * @param SecurityProvider
     *            the SecurityProvider from which to obtain the Security
     * @return the Security held by this Position
     */
    public Security getSecurity(SecurityProvider securityProvider) {
        return securityProvider.getSecurity(securityKey);
    }

    /**
     * Obtains the key of the Security held by this Position.
     *
     * @return the key of the Security held by this Position
     */
    public SecurityKey getSecurityKey() {
        return securityKey;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
