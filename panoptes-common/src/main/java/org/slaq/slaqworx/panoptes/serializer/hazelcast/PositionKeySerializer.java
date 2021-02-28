package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.asset.PositionKey;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link PositionKey}.
 *
 * @author jeremy
 */
public class PositionKeySerializer extends HazelcastStreamSerializer<PositionKey> {
  /**
   * Creates a new {@link PositionKeySerializer}. Hazelcast requires a public default constructor.
   */
  public PositionKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PositionKeySerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.POSITION_KEY.ordinal();
  }
}
