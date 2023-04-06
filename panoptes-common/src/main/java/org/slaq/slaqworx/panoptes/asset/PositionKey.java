package org.slaq.slaqworx.panoptes.asset;

import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * A key used to reference {@link Position}s.
 *
 * @param id the ID to assign to the key, or {@code null} to generate one
 * @author jeremy
 */
public record PositionKey(@Nonnull String id) implements ProtobufSerializable {
  /**
   * Creates a new {@link PositionKey} with the given ID.
   *
   * @param id the ID to assign to the key, or {@code null} to generate one
   */
  public PositionKey(String id) {
    this.id = (id == null ? IdVersionKey.generateId() : id);
  }

  @Override
  @Nonnull
  public String toString() {
    return id;
  }
}
