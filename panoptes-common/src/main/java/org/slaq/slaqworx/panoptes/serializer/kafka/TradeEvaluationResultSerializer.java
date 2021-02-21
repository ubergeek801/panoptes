package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code TradeEvaluationResult}.
 *
 * @author jeremy
 */
public class TradeEvaluationResultSerializer extends KafkaSerializer<TradeEvaluationResult> {
    /**
     * Creates a new {@code TradeEvaluationResultSerializer}. Kafka requires a public default
     * constructor.
     */
    public TradeEvaluationResultSerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.TradeEvaluationResultSerializer());
    }
}
