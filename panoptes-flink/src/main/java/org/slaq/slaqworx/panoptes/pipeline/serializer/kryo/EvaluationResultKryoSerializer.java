package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.serializer.EvaluationResultSerializer;

public class EvaluationResultKryoSerializer extends ProtobufKryoSerializer<EvaluationResult> {
    public EvaluationResultKryoSerializer() {
        // nothing to do
    }

    @Override
    protected EvaluationResultSerializer createProtobufSerializer() {
        return new EvaluationResultSerializer();
    }
}
