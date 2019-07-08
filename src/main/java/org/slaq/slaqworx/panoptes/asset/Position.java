package org.slaq.slaqworx.panoptes.asset;

import java.util.UUID;

/**
 * A Position is a holding of some amount of a particular Security by some Portfolio.
 *
 * @author jeremy
 */
public class Position {
    private final String id;
    private final double amount;
    private final Security security;

    /**
     * Creates a new Position with a generated ID and the given amount and Security.
     * 
     * @param amount   the amount of the Security held in this Position
     * @param security the held Security
     */
    public Position(double amount, Security security) {
        this(UUID.randomUUID().toString(), amount, security);
    }

    /**
     * Creates a new Position with the given ID, amount and Security.
     *
     * @param id       the unique ID to assign to this Position
     * @param amount   the amount of the Security held in this Position
     * @param security the held Security
     */
    public Position(String id, double amount, Security security) {
        this.id = id;
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
        Position other = (Position) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
}
