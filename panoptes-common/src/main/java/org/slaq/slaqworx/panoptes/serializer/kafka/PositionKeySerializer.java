package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.PositionKey;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link PositionKey}.
 *
 * @author jeremy
 */
public class PositionKeySerializer extends KafkaSerializer<PositionKey> {
  /** Creates a new {@link PositionKeySerializer}. Kafka requires a public default constructor. */
  public PositionKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PositionKeySerializer());
  }
}
