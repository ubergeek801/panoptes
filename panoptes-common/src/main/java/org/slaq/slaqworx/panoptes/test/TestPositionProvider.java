package org.slaq.slaqworx.panoptes.test;

import java.util.HashMap;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.PositionProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;

/**
 * {@code TestPositionProvider} is a {@code PositionProvider} suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestPositionProvider implements PositionProvider {
    private final HashMap<PositionKey, Position> positionMap = new HashMap<>();

    /**
     * Creates a new {@code TestPositionProvider}. Restricted because instances of this class should
     * be obtained through {@code TestUtil}.
     */
    protected TestPositionProvider() {
        // nothing to do
    }

    @Override
    public Position getPosition(PositionKey key) {
        return positionMap.get(key);
    }

    /**
     * Creates a new {@code Position} and makes it available through this provider.
     *
     * @param id
     *            the ID of the {@code Position} to create, or {@code null} to generate an ID
     * @param amount
     *            the amount of the {@code Position}
     * @param security
     *            the {@code Security} held by the {@code Position}
     * @return the newly created {@code Position}
     */
    public Position newPosition(String id, double amount, Security security) {
        Position position = new SimplePosition(new PositionKey(id), amount, security.getKey());
        positionMap.put(position.getKey(), position);

        return position;
    }
}
