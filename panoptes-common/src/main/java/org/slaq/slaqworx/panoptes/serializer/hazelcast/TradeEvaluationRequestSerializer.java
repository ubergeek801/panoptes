package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link
 * TradeEvaluationRequest}.
 *
 * @author jeremy
 */
public class TradeEvaluationRequestSerializer
    extends HazelcastStreamSerializer<TradeEvaluationRequest> {
  /**
   * Creates a new {@link TradeEvaluationRequestSerializer}. Hazelcast requires a public default
   * constructor.
   */
  public TradeEvaluationRequestSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TradeEvaluationRequestSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.TRADE_EVALUATION_REQUEST.ordinal();
  }
}
