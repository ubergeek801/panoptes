package org.slaq.slaqworx.panoptes.asset;

/**
 * A partial implementation of {@link Position} which provides {@code equals()} and {@code
 * hashCode()} semantics based on the {@link Position}'s key.
 *
 * @author jeremy
 */
public abstract class AbstractPosition implements Position {
  private final PositionKey key;

  /**
   * Creates a new {@link AbstractPosition} with a generated key.
   */
  protected AbstractPosition() {
    this(null);
  }

  /**
   * Creates a new {@link AbstractPosition} with the specified key.
   *
   * @param key
   *     the {@link PositionKey} identifying this {@link Position}
   */
  protected AbstractPosition(PositionKey key) {
    this.key = (key == null ? new PositionKey(null) : key);
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
    Position other = (Position) obj;

    return key.equals(other.getKey());
  }

  @Override
  public PositionKey getKey() {
    return key;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }
}
