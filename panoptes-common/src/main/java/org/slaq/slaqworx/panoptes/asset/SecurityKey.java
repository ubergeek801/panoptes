package org.slaq.slaqworx.panoptes.asset;

import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * A key used to reference {@link Security} entities.
 *
 * @param id the ID to assign to the key
 * @author jeremy
 */
public record SecurityKey(@Nonnull String id)
    implements Comparable<SecurityKey>, ProtobufSerializable {
  @Override
  public int compareTo(@Nonnull SecurityKey other) {
    return id.compareTo(other.id());
  }

  @Override
  @Nonnull
  public String toString() {
    return id;
  }
}
