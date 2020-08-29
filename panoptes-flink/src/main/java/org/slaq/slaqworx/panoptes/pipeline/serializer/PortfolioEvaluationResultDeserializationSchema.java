package org.slaq.slaqworx.panoptes.pipeline.serializer;

import org.apache.flink.api.common.typeinfo.TypeInformation;

import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.serializer.EvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

public class PortfolioEvaluationResultDeserializationSchema
        extends ProtobufDeserializationSchema<EvaluationResult> {
    private static final long serialVersionUID = 1L;

    public PortfolioEvaluationResultDeserializationSchema() {
        // nothing to do
    }

    @Override
    public TypeInformation<EvaluationResult> getProducedType() {
        return TypeInformation.of(EvaluationResult.class);
    }

    @Override
    protected ProtobufSerializer<EvaluationResult> createSerializer() {
        return new EvaluationResultSerializer();
    }
}
