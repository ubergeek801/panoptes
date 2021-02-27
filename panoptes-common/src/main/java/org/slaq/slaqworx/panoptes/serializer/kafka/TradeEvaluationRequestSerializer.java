package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code TradeEvaluationRequest}.
 *
 * @author jeremy
 */
public class TradeEvaluationRequestSerializer extends KafkaSerializer<TradeEvaluationRequest> {
  /**
   * Creates a new {@code TradeEvaluationRequestSerializer}. Kafka requires a public default
   * constructor.
   */
  public TradeEvaluationRequestSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TradeEvaluationRequestSerializer());
  }
}
