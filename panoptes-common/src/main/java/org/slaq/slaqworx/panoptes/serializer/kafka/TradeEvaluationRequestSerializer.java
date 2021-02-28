package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link TradeEvaluationRequest}.
 *
 * @author jeremy
 */
public class TradeEvaluationRequestSerializer extends KafkaSerializer<TradeEvaluationRequest> {
  /**
   * Creates a new {@link TradeEvaluationRequestSerializer}. Kafka requires a public default
   * constructor.
   */
  public TradeEvaluationRequestSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TradeEvaluationRequestSerializer());
  }
}
