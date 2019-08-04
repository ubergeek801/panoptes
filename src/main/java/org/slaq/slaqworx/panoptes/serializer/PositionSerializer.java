package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PositionMsg;

public class PositionSerializer implements ByteArraySerializer<MaterializedPosition> {
    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.POSITION.ordinal();
    }

    @Override
    public MaterializedPosition read(byte[] buffer) throws IOException {
        PositionMsg positionMsg = PositionMsg.parseFrom(buffer);
        IdKeyMsg keyMsg = positionMsg.getKey();
        PositionKey key = new PositionKey(keyMsg.getId());
        IdKeyMsg securityKeyMsg = positionMsg.getSecurityKey();
        SecurityKey securityKey = new SecurityKey(securityKeyMsg.getId());

        return new MaterializedPosition(key, positionMsg.getAmount(), securityKey);
    }

    @Override
    public byte[] write(MaterializedPosition position) throws IOException {
        IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
        keyBuilder.setId(position.getKey().getId());
        IdKeyMsg key = keyBuilder.build();

        IdKeyMsg.Builder securityKeyBuilder = IdKeyMsg.newBuilder();
        securityKeyBuilder.setId(position.getSecurityKey().getId());
        IdKeyMsg securityKey = securityKeyBuilder.build();

        PositionMsg.Builder positionBuilder = PositionMsg.newBuilder();
        positionBuilder.setKey(key);
        positionBuilder.setAmount(position.getAmount());
        positionBuilder.setSecurityKey(securityKey);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        positionBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
