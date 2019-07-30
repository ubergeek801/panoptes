package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A Position is a holding of some amount of a particular Security by some Portfolio.
 *
 * @author jeremy
 */
@Entity
public class Position implements Keyed<PositionKey>, Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private final PositionKey id;
    private final double amount;
    private final SecurityKey securityId;

    /**
     * Creates a new Position with a generated key and the given amount and Security.
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
     *            the unique key to assign to this Position, or null to generate one
     * @param amount
     *            the amount of the Security held in this Position
     * @param security
     *            the held Security
     */
    public Position(PositionKey id, double amount, Security security) {
        this.id = (id == null ? new PositionKey(null, 1) : id);
        this.amount = amount;
        securityId = security.getId();
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

    @Override
    public PositionKey getId() {
        return id;
    }

    /**
     * Obtains the Security held by this Position.
     *
     * @param SecurityProvider
     *            the SecurityProvider from which to obtain the Security
     * @return the Security held by this Position
     */
    public Security getSecurity(SecurityProvider securityProvider) {
        return securityProvider.getSecurity(securityId);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
