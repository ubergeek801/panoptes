package org.slaq.slaqworx.panoptes.pipeline.serializer;

import org.apache.flink.api.common.typeinfo.TypeInformation;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.serializer.PortfolioSerializer;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

public class PortfolioDeserializationSchema extends ProtobufDeserializationSchema<Portfolio> {
    private static final long serialVersionUID = 1L;

    public PortfolioDeserializationSchema() {
        // nothing to do
    }

    @Override
    public TypeInformation<Portfolio> getProducedType() {
        return TypeInformation.of(Portfolio.class);
    }

    @Override
    protected ProtobufSerializer<Portfolio> createSerializer() {
        return new PortfolioSerializer(null);
    }
}
