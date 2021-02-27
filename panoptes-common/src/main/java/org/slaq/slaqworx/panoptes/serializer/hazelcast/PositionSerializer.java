package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a {@code Position}.
 *
 * @author jeremy
 */
public class PositionSerializer extends HazelcastStreamSerializer<Position> {
  /**
   * Creates a new {@code PositionSerializer}. Hazelcast requires a public default constructor.
   */
  public PositionSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PositionSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.POSITION.ordinal();
  }
}
