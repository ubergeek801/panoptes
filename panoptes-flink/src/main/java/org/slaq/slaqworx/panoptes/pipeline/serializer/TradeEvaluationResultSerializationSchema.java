package org.slaq.slaqworx.panoptes.pipeline.serializer;

import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeEvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;

public class TradeEvaluationResultSerializationSchema
        extends ProtobufSerializationSchema<TradeEvaluationResult> {
    private static final long serialVersionUID = 1L;

    public TradeEvaluationResultSerializationSchema(String topic) {
        super(topic);
    }

    @Override
    protected ProtobufSerializer<TradeEvaluationResult> createSerializer() {
        return new TradeEvaluationResultSerializer();
    }
}
