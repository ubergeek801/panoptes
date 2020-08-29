package org.slaq.slaqworx.panoptes.pipeline.serializer;

import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.serializer.EvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

public class PortfolioEvaluationResultSerializationSchema
        extends ProtobufSerializationSchema<EvaluationResult> {
    private static final long serialVersionUID = 1L;

    public PortfolioEvaluationResultSerializationSchema(String topic) {
        super(topic);
    }

    @Override
    protected ProtobufSerializer<EvaluationResult> createSerializer() {
        return new EvaluationResultSerializer();
    }
}
