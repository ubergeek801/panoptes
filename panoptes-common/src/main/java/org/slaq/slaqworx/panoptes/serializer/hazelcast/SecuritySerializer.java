package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.asset.Security;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link Security}.
 *
 * @author jeremy
 */
public class SecuritySerializer extends HazelcastStreamSerializer<Security> {
  /** Creates a new {@link SecuritySerializer}. Hazelcast requires a public default constructor. */
  public SecuritySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.SecuritySerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.SECURITY.ordinal();
  }
}
