package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;
import java.util.UUID;

/**
 * A Position is a holding of some amount of a particular Security by some Portfolio.
 *
 * @author jeremy
 */
public class Position implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final double amount;
    private final Security security;

    /**
     * Creates a new Position with a generated ID and the given amount and Security, and a generated
     * ID.
     *
     * @param amount
     *            the amount of the Security held in this Position
     * @param security
     *            the held Security
     */
    public Position(double amount, Security security) {
        this(null, amount, security);
    }

    /**
     * Creates a new Position with the given ID, amount and Security.
     *
     * @param id
     *            the unique ID to assign to this Position, or null to generate one
     * @param amount
     *            the amount of the Security held in this Position
     * @param security
     *            the held Security
     */
    public Position(String id, double amount, Security security) {
        this.id = (id == null ? UUID.randomUUID().toString() : id);
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        Position other = (Position)obj;
        return id.equals(other.id);
    }

    /**
     * Obtains the amount of this Position.
     *
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Obtains the unique ID of this Position.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Obtains the Security held by this Position.
     *
     * @return the Security
     */
    public Security getSecurity() {
        return security;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
