package org.slaq.slaqworx.panoptes.asset;

/**
 * A MaterializedPosition is a basic implementation of the Position interface. A
 * MaterializedPosition may be durable (e.g. sourced from a database/cache) or ephemeral (e.g.
 * supplied by a proposed Trade or even a unit test).
 *
 * @author jeremy
 */
public class MaterializedPosition implements Position {
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
    public MaterializedPosition(double amount, SecurityKey securityKey) {
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
    public MaterializedPosition(PositionKey key, double amount, SecurityKey securityKey) {
        this.key = (key == null ? new PositionKey(null) : key);
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
        if (!(obj instanceof Position)) {
            return false;
        }
        Position other = (Position)obj;
        return key.equals(other.getKey());
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public PositionKey getKey() {
        return key;
    }

    @Override
    public Security getSecurity(SecurityProvider securityProvider) {
        return securityProvider.getSecurity(securityKey);
    }

    @Override
    public SecurityKey getSecurityKey() {
        return securityKey;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
