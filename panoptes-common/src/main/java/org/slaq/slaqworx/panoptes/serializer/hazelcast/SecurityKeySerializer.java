package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link SecurityKey}.
 *
 * @author jeremy
 */
public class SecurityKeySerializer extends HazelcastStreamSerializer<SecurityKey> {
  /**
   * Creates a new {@link SecurityKeySerializer}. Hazelcast requires a public default constructor.
   */
  public SecurityKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.SecurityKeySerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.SECURITY_KEY.ordinal();
  }
}
