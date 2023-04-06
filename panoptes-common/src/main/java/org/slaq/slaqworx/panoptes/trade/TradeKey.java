package org.slaq.slaqworx.panoptes.trade;

import org.slaq.slaqworx.panoptes.asset.IdVersionKey;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * A key used to reference {@link Trade}s.
 *
 * @author jeremy
 */
public class TradeKey implements ProtobufSerializable {
  private final String id;

  /**
   * Creates a new {@link TradeKey} with the given ID.
   *
   * @param id the ID to assign to the key, or {@code null} to generate one
   */
  public TradeKey(String id) {
    this.id = (id == null ? IdVersionKey.generateId() : id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TradeKey other = (TradeKey) obj;

    return id.equals(other.id);
  }

  /**
   * Obtains this {@link TradeKey}'s ID.
   *
   * @return the ID underlying this key
   */
  public String getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return id;
  }
}
