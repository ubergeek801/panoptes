package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link TradeEvaluationResult}.
 *
 * @author jeremy
 */
public class TradeEvaluationResultSerializer extends KafkaSerializer<TradeEvaluationResult> {
  /**
   * Creates a new {@link TradeEvaluationResultSerializer}. Kafka requires a public default
   * constructor.
   */
  public TradeEvaluationResultSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TradeEvaluationResultSerializer());
  }
}
