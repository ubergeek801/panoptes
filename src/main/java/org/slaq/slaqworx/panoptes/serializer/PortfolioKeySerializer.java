package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;

public class PortfolioKeySerializer implements ByteArraySerializer<PortfolioKey> {
    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.PORTFOLIO_KEY.ordinal();
    }

    @Override
    public PortfolioKey read(byte[] buffer) throws IOException {
        IdVersionKeyMsg keyMsg = IdVersionKeyMsg.parseFrom(buffer);
        return new PortfolioKey(keyMsg.getId(), keyMsg.getVersion());
    }

    @Override
    public byte[] write(PortfolioKey key) throws IOException {
        IdVersionKeyMsg.Builder keyBuilder = IdVersionKeyMsg.newBuilder();
        keyBuilder.setId(key.getId());
        keyBuilder.setVersion(key.getVersion());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        keyBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
