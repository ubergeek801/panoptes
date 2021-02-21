package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.trade.RoomEvaluationRequest;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code RoomEvaluationRequest}.
 *
 * @author jeremy
 */
public class RoomEvaluationRequestSerializer extends KafkaSerializer<RoomEvaluationRequest> {
    /**
     * Creates a new {@code RoomEvaluationRequestSerializer}. Kafka requires a public default
     * constructor.
     */
    public RoomEvaluationRequestSerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.RoomEvaluationRequestSerializer());
    }
}
