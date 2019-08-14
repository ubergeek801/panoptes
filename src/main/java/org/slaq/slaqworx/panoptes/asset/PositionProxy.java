package org.slaq.slaqworx.panoptes.asset;

/**
 * A {@code PositionProxy} is a proxy for a concrete {@code Position} implementation, where only the
 * key is known at creation time. Subsequent operations on the {@code PositionProxy} are delegated
 * to the {@code Position} resolved by the specified {@code PositionProvider}.
 *
 * @author jeremy
 */
public class PositionProxy implements Position {
    private final PositionKey key;
    private MaterializedPosition position;
    private final PositionProvider positionProvider;

    /**
     * Creates a new {@code PositionProxy} of the given key, delegating to the given
     * {@code Position} provider.
     *
     * @param key
     *            the key of the proxied {@code Position}
     * @param positionProvider
     *            the {@code PositionProvider} by which to resolve the {@code Position}
     */
    public PositionProxy(PositionKey key, PositionProvider positionProvider) {
        this.key = key;
        this.positionProvider = positionProvider;
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
        return getPosition().getAmount();
    }

    @Override
    public PositionKey getKey() {
        return key;
    }

    @Override
    public Security getSecurity(SecurityProvider securityProvider) {
        return getPosition().getSecurity(securityProvider);
    }

    @Override
    public SecurityKey getSecurityKey() {
        return getPosition().getSecurityKey();
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * Obtains the {@code Position} proxied by this {@code PositionProxy}.
     *
     * @return the proxied {@code Position}
     */
    protected Position getPosition() {
        if (position == null) {
            position = positionProvider.getPosition(key);
        }

        return position;
    }
}
