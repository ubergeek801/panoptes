package org.slaq.slaqworx.panoptes;

import java.util.HashMap;

import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.PositionProvider;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * TestSecurityPTestPositionProviderrovider is a PositionProvider suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestPositionProvider implements PositionProvider {
    private final HashMap<PositionKey, Position> positionMap = new HashMap<>();

    /**
     * Creates a new TestPositionProvider. Restricted because instances of this class should be
     * obtained through TestUtil.
     */
    protected TestPositionProvider() {
        // nothing to do
    }

    @Override
    public Position getPosition(PositionKey key) {
        return positionMap.get(key);
    }

    /**
     * Creates a new Position and makes it available through this provider.
     *
     * @param id
     *            the ID of the Position to create, or null to generate an ID
     * @param amount
     *            the amount of the Position
     * @param securityKey
     *            the key identifying the Security held by the Position
     * @return the newly created Position
     */
    public Position newPosition(String id, double amount, SecurityKey securityKey) {
        MaterializedPosition position =
                new MaterializedPosition(new PositionKey(id), amount, securityKey);
        positionMap.put(position.getKey(), position);

        return position;
    }
}
