package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code RuleKeySerializer} (de)serializes the state of a {@code RuleKey} using Protobuf.
 *
 * @author jeremy
 */
public class RuleKeySerializer implements ByteArraySerializer<RuleKey> {
    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.RULE_KEY.ordinal();
    }

    @Override
    public RuleKey read(byte[] buffer) throws IOException {
        IdVersionKeyMsg keyMsg = IdVersionKeyMsg.parseFrom(buffer);
        return new RuleKey(keyMsg.getId());
    }

    @Override
    public byte[] write(RuleKey key) throws IOException {
        IdVersionKeyMsg.Builder keyBuilder = IdVersionKeyMsg.newBuilder();
        keyBuilder.setId(key.getId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        keyBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
