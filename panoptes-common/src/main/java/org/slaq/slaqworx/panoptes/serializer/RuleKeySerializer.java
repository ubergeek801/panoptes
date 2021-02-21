package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code RuleKey}.
 *
 * @author jeremy
 */
public class RuleKeySerializer implements ProtobufSerializer<RuleKey> {
    /**
     * Creates a new {@code RuleKeySerializer}.
     */
    public RuleKeySerializer() {
        // nothing to do
    }

    @Override
    public RuleKey read(byte[] buffer) throws IOException {
        IdKeyMsg keyMsg = IdKeyMsg.parseFrom(buffer);
        return new RuleKey(keyMsg.getId());
    }

    @Override
    public byte[] write(RuleKey key) throws IOException {
        IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
        keyBuilder.setId(key.getId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        keyBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
