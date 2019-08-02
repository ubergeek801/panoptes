package org.slaq.slaqworx.panoptes.asset;

/**
 * A PositionProxy is a proxy for a concrete Position implementation, where only the key is known at
 * creation time. Subsequent operations on the PositionProxy are delegated to the position resolved
 * by the specified PositionProvider.
 *
 * @author jeremy
 */
public class PositionProxy implements Position {
    private static final long serialVersionUID = 1L;

    private final PositionKey key;
    private final transient PositionProvider positionProvider;

    /**
     * Creates a new PositionProxy of the given key, delegating to the given Position provider.
     *
     * @param key
     *            the key of the proxied Position
     * @param positionProvider
     *            the PositionProvider by which to resolve the Position
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
     * Obtains the Position proxied by this PositionProxy.
     *
     * @return the proxied Position
     */
    protected Position getPosition() {
        return positionProvider.getPosition(key);
    }
}
