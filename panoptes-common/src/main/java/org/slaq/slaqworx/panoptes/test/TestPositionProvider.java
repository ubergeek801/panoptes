package org.slaq.slaqworx.panoptes.test;

import java.util.HashMap;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.PositionProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;

/**
 * A {@link PositionProvider} suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestPositionProvider implements PositionProvider {
  private final HashMap<PositionKey, Position> positionMap = new HashMap<>();

  /**
   * Creates a new {@link TestPositionProvider}. Restricted because instances of this class should
   * be obtained through {@link TestUtil}.
   */
  protected TestPositionProvider() {
    // nothing to do
  }

  @Override
  public Position getPosition(PositionKey key) {
    return positionMap.get(key);
  }

  /**
   * Creates a new {@link Position} and makes it available through this provider.
   *
   * @param id
   *     the ID of the {@link Position} to create, or {@code null} to generate an ID
   * @param amount
   *     the amount of the {@link Position}
   * @param security
   *     the {@link Security} held by the {@link Position}
   *
   * @return the newly created {@link Position}
   */
  public Position newPosition(String id, double amount, Security security) {
    Position position = new SimplePosition(new PositionKey(id), amount, security.getKey());
    positionMap.put(position.getKey(), position);

    return position;
  }
}
