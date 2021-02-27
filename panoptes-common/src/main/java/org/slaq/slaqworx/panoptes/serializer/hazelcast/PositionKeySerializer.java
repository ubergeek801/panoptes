package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.asset.PositionKey;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a {@code PositionKey}.
 *
 * @author jeremy
 */
public class PositionKeySerializer extends HazelcastStreamSerializer<PositionKey> {
  /**
   * Creates a new {@code PositionKeySerializer}. Hazelcast requires a public default constructor.
   */
  public PositionKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PositionKeySerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.POSITION_KEY.ordinal();
  }
}
