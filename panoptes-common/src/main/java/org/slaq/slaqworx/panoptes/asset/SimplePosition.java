package org.slaq.slaqworx.panoptes.asset;

/**
 * A {@code Position} that is somewhat of a contrivance in that it is constructed "out of thin air,"
 * in contrast to a "real" {@code Position} which, in a {@code Portfolio}, is actually an
 * aggregation of {@code TaxLot}s derived from {@code Transaction}s against the same
 * {@code Security}. {@code SimplePosition} may be used to represent benchmark weights or other
 * artificially-constructed {@code Position}-like entities.
 *
 * @author jeremy
 */
public class SimplePosition extends AbstractPosition {
    private final double amount;
    private final SecurityKey securityKey;

    /**
     * Creates a new {@code SimplePosition} with a generated key and the specified amount and
     * {@code Security}.
     *
     * @param amount
     *            the amount of the {@code Security} held in this {@code Position}
     * @param securityKey
     *            a {@code SecurityKey} identifying the held {@code Security}
     */
    public SimplePosition(double amount, SecurityKey securityKey) {
        this(null, amount, securityKey);
    }

    /**
     * Creates a new {@code SimplePosition} with the specified key, amount and {@code Security}.
     *
     * @param key
     *            the unique key to assign to this {@code Position}, or {@code null} to generate one
     * @param amount
     *            the amount of the {@code Security} held in this {@code Position}
     * @param securityKey
     *            a {@code SecurityKey} identifying the held {@code Security}
     */
    public SimplePosition(PositionKey key, double amount, SecurityKey securityKey) {
        super(key);
        this.amount = amount;
        this.securityKey = securityKey;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public SecurityKey getSecurityKey() {
        return securityKey;
    }
}
