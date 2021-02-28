package org.slaq.slaqworx.panoptes.asset;

import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * A key used to reference {@link Security} entities.
 *
 * @author jeremy
 */
public class SecurityKey implements Comparable<SecurityKey>, ProtobufSerializable {
  private final String id;

  /**
   * Creates a new {@link SecurityKey} with the given ID.
   *
   * @param id
   *     the ID to assign to the key
   */
  public SecurityKey(String id) {
    this.id = id;
  }

  @Override
  public int compareTo(SecurityKey other) {
    return id.compareTo(other.getId());
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
    SecurityKey other = (SecurityKey) obj;

    return id.equals(other.id);
  }

  /**
   * Obtains this {@link SecurityKey}'s ID.
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
