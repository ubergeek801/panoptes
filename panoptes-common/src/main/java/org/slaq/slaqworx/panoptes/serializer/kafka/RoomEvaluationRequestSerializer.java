package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.trade.RoomEvaluationRequest;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link RoomEvaluationRequest}.
 *
 * @author jeremy
 */
public class RoomEvaluationRequestSerializer extends KafkaSerializer<RoomEvaluationRequest> {
  /**
   * Creates a new {@link RoomEvaluationRequestSerializer}. Kafka requires a public default
   * constructor.
   */
  public RoomEvaluationRequestSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.RoomEvaluationRequestSerializer());
  }
}
