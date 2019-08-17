package org.slaq.slaqworx.panoptes;

import java.util.HashMap;

import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.PositionProvider;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * {@code TestPositionProvider} is a {@code PositionProvider} suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestPositionProvider implements PositionProvider {
    private final HashMap<PositionKey, MaterializedPosition> positionMap = new HashMap<>();

    /**
     * Creates a new {@code TestPositionProvider}. Restricted because instances of this class should
     * be obtained through {@code TestUtil}.
     */
    protected TestPositionProvider() {
        // nothing to do
    }

    @Override
    public MaterializedPosition getPosition(PositionKey key) {
        return positionMap.get(key);
    }

    /**
     * Creates a new {@code Position} and makes it available through this provider.
     *
     * @param id
     *            the ID of the {@code Position} to create, or {@code null} to generate an ID
     * @param amount
     *            the amount of the {@code Position}
     * @param securityKey
     *            the key identifying the {@code Security} held by the {@code Position}
     * @return the newly created {@code Position}
     */
    public Position newPosition(String id, double amount, SecurityKey securityKey) {
        MaterializedPosition position =
                new MaterializedPosition(new PositionKey(id), amount, securityKey);
        positionMap.put(position.getKey(), position);

        return position;
    }
}
