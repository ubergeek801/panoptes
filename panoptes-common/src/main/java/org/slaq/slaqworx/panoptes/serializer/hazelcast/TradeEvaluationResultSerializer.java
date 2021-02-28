package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link
 * TradeEvaluationResult}.
 *
 * @author jeremy
 */
public class TradeEvaluationResultSerializer
    extends HazelcastStreamSerializer<TradeEvaluationResult> {
  /**
   * Creates a new {@link TradeEvaluationResultSerializer}. Hazelcast requires a public default
   * constructor.
   */
  public TradeEvaluationResultSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TradeEvaluationResultSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.TRADE_EVALUATION_RESULT.ordinal();
  }
}
