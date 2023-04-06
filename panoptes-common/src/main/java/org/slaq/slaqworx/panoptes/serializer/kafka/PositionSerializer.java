package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link Position}.
 *
 * @author jeremy
 */
public class PositionSerializer extends KafkaSerializer<Position> {
  /** Creates a new {@link PositionSerializer}. Kafka requires a public default constructor. */
  public PositionSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PositionSerializer());
  }
}
