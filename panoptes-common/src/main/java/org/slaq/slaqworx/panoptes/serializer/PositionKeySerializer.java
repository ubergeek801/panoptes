package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code PositionKey}.
 *
 * @author jeremy
 */
public class PositionKeySerializer implements ProtobufSerializer<PositionKey> {
    /**
     * Creates a new {@code PositionKeySerializer}.
     */
    public PositionKeySerializer() {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.POSITION_KEY.ordinal();
    }

    @Override
    public PositionKey read(byte[] buffer) throws IOException {
        IdKeyMsg keyMsg = IdKeyMsg.parseFrom(buffer);
        return new PositionKey(keyMsg.getId());
    }

    @Override
    public byte[] write(PositionKey key) throws IOException {
        IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
        keyBuilder.setId(key.getId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        keyBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
