package org.slaq.slaqworx.panoptes.pipeline.serializer;

import org.apache.flink.api.common.typeinfo.TypeInformation;

import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.serializer.PortfolioEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

public class PortfolioEvaluationRequestDeserializationSchema
        extends ProtobufDeserializationSchema<PortfolioEvaluationRequest> {
    private static final long serialVersionUID = 1L;

    public PortfolioEvaluationRequestDeserializationSchema() {
        // nothing to do
    }

    @Override
    public TypeInformation<PortfolioEvaluationRequest> getProducedType() {
        return TypeInformation.of(PortfolioEvaluationRequest.class);
    }

    @Override
    protected ProtobufSerializer<PortfolioEvaluationRequest> createSerializer() {
        return new PortfolioEvaluationRequestSerializer();
    }
}
