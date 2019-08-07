package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;

/**
 * <code>SecurityKeySerializer</code> (de)serializes the state of a <code>SecurityKey</code> using
 * Protobuf.
 *
 * @author jeremy
 */
public class SecurityKeySerializer implements ByteArraySerializer<SecurityKey> {
    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.SECURITY_KEY.ordinal();
    }

    @Override
    public SecurityKey read(byte[] buffer) throws IOException {
        IdVersionKeyMsg keyMsg = IdVersionKeyMsg.parseFrom(buffer);
        return new SecurityKey(keyMsg.getId());
    }

    @Override
    public byte[] write(SecurityKey key) throws IOException {
        IdVersionKeyMsg.Builder keyBuilder = IdVersionKeyMsg.newBuilder();
        keyBuilder.setId(key.getId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        keyBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
