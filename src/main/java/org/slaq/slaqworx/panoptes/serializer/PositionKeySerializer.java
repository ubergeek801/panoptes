package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;

public class PositionKeySerializer implements ByteArraySerializer<PositionKey> {
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
        IdVersionKeyMsg keyMsg = IdVersionKeyMsg.parseFrom(buffer);
        return new PositionKey(keyMsg.getId());
    }

    @Override
    public byte[] write(PositionKey key) throws IOException {
        IdVersionKeyMsg.Builder keyBuilder = IdVersionKeyMsg.newBuilder();
        keyBuilder.setId(key.getId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        keyBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
