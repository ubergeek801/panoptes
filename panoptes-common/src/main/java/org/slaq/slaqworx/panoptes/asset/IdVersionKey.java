package org.slaq.slaqworx.panoptes.asset;

import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.builder.CompareToBuilder;

/**
 * A key used to reference some asset types (using trivial subclasses).
 *
 * @author jeremy
 */
public abstract class IdVersionKey implements Comparable<IdVersionKey> {
  private final String id;
  private final long version;

  /**
   * Creates a new {@link IdVersionKey} with the given ID and version.
   *
   * @param id
   *     the ID to assign to the key, or {@code null} to generate one
   * @param version
   *     the version to assign to the key
   */
  public IdVersionKey(String id, long version) {
    this.id = (id == null ? generateId() : id);
    this.version = version;
  }

  /**
   * Obtains a generated ID of the form used by an {@link IdVersionKey}.
   *
   * @return a generated ID
   */
  public static String generateId() {
    return UUID.randomUUID().toString();
  }

  @Override
  public int compareTo(IdVersionKey o) {
    return new CompareToBuilder().append(id, o.id).append(version, o.version).toComparison();
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
    IdVersionKey other = (IdVersionKey) obj;

    return Objects.equals(id, other.id) && version == other.version;
  }

  /**
   * Obtains this key's ID.
   *
   * @return the ID portion of the key
   */
  public String getId() {
    return id;
  }

  /**
   * Obtains this key's version.
   *
   * @return the version portion of the key
   */
  public long getVersion() {
    return version;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, version);
  }

  @Override
  public String toString() {
    return id + ":" + version;
  }
}
