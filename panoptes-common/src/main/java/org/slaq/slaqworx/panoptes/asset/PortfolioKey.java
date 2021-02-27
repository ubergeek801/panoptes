package org.slaq.slaqworx.panoptes.asset;

import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * A key used to reference {@code Portfolio}s.
 *
 * @author jeremy
 */
public class PortfolioKey extends IdVersionKey implements ProtobufSerializable {
  /**
   * Creates a new {@code PortfolioKey} with the given ID and version.
   *
   * @param id
   *     the ID to assign to the key, or {@code null} to generate one
   * @param version
   *     the version to assign to the key
   */
  public PortfolioKey(String id, long version) {
    super(id, version);
  }
}
