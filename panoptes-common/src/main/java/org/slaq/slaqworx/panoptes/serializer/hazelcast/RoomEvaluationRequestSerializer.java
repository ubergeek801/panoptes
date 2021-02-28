package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.trade.RoomEvaluationRequest;

/**
 * A {@link HazelcastStreamSerializer} wihch (de)serializes the state of a {@link
 * RoomEvaluationRequest}.
 *
 * @author jeremy
 */
public class RoomEvaluationRequestSerializer
    extends HazelcastStreamSerializer<RoomEvaluationRequest> {
  /**
   * Creates a new {@link RoomEvaluationRequestSerializer}. Hazelcast requires a public default
   * constructor.
   */
  public RoomEvaluationRequestSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.RoomEvaluationRequestSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.ROOM_EVALUATION_REQUEST.ordinal();
  }
}
