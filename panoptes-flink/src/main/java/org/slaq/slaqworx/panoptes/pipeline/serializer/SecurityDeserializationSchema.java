package org.slaq.slaqworx.panoptes.pipeline.serializer;

import org.apache.flink.api.common.typeinfo.TypeInformation;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;
import org.slaq.slaqworx.panoptes.serializer.SecuritySerializer;

public class SecurityDeserializationSchema extends ProtobufDeserializationSchema<Security> {
    private static final long serialVersionUID = 1L;

    public SecurityDeserializationSchema() {
        // nothing to do
    }

    @Override
    public TypeInformation<Security> getProducedType() {
        return TypeInformation.of(Security.class);
    }

    @Override
    protected ProtobufSerializer<Security> createSerializer() {
        return new SecuritySerializer();
    }
}
