package org.slaq.slaqworx.panoptes.trade;

import org.slaq.slaqworx.panoptes.asset.IdVersionKey;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * A key used to reference {@code Transaction}s.
 *
 * @author jeremy
 */
public class TransactionKey implements ProtobufSerializable {
  private final String id;

  /**
   * Creates a new {@code TransactionKey} with the given ID.
   *
   * @param id
   *     the ID to assign to the key, or {@code null} to generate one
   */
  public TransactionKey(String id) {
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
    TransactionKey other = (TransactionKey) obj;

    return id.equals(other.id);
  }

  /**
   * Obtains this {@code TransactionKey}'s ID.
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
