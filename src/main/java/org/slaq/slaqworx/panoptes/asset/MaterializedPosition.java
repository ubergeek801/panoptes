package org.slaq.slaqworx.panoptes.asset;

/**
 * A {@code MaterializedPosition} is a basic implementation of the {@code Position} interface. A
 * {@code MaterializedPosition} may be durable (e.g. sourced from a database/cache) or ephemeral
 * (e.g. supplied by a proposed {@code Trade} or even a unit test).
 *
 * @author jeremy
 */
public class MaterializedPosition implements Position {
    private final PositionKey key;
    private final double amount;
    private final SecurityKey securityKey;
    private Security security;

    /**
     * Creates a new {@code MaterializedPosition} with a generated key and the specified amount and
     * {@code Security}.
     *
     * @param amount
     *            the amount of the {@code Security} held in this {@code Position}
     * @param securityKey
     *            the key of the held {@code Security}
     */
    public MaterializedPosition(double amount, SecurityKey securityKey) {
        this(null, amount, securityKey);
    }

    /**
     * Creates a new {@code MaterializedPosition} with the specified ID, amount and
     * {@code Security}.
     *
     * @param key
     *            the unique key to assign to this {@code MaterializedPosition}, or {@code null} to
     *            generate one
     * @param amount
     *            the amount of the {@code Security} held in this {@code Position}
     * @param securityKey
     *            the key of the held {@code Security}
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
        if (security == null) {
            security = securityProvider.getSecurity(securityKey);
        }

        return security;
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
