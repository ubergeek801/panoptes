package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code PortfolioKey}.
 *
 * @author jeremy
 */
public class PortfolioKeySerializer implements ProtobufSerializer<PortfolioKey> {
    /**
     * Creates a new {@code PortfolioKeySerializer}.
     */
    public PortfolioKeySerializer() {
        // nothing to do
    }

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
