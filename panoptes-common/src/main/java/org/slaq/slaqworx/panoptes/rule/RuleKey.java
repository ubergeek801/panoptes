package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.IdVersionKey;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * A key used to reference {@link Rule}s.
 *
 * @param id
 *     the ID to assign to the key, or {@code null} to generate one
 *
 * @author jeremy
 */
public record RuleKey(String id) implements ProtobufSerializable {
  /**
   * Creates a new {@link RuleKey} with the given ID.
   *
   * @param id
   *     the ID to assign to the key, or {@code null} to generate one
   */
  public RuleKey(String id) {
    this.id = (id == null ? IdVersionKey.generateId() : id);
  }

  @Override
  public String toString() {
    return id;
  }
}
