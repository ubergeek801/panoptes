package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.serializer.PortfolioSerializer;

public class PortfolioKryoSerializer extends ProtobufKryoSerializer<Portfolio> {
    public PortfolioKryoSerializer() {
        // nothing to do
    }

    @Override
    protected PortfolioSerializer createProtobufSerializer() {
        return new PortfolioSerializer();
    }
}
