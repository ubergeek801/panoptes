package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.trade.TransactionKey;

/**
 * {@code TransactionKeySerializer} (de)serializes the state of a {@code TransactionKey} using
 * Protobuf.
 *
 * @author jeremy
 */
public class TransactionKeySerializer implements ByteArraySerializer<TransactionKey> {
    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.TRANSACTION_KEY.ordinal();
    }

    @Override
    public TransactionKey read(byte[] buffer) throws IOException {
        IdKeyMsg keyMsg = IdKeyMsg.parseFrom(buffer);
        return new TransactionKey(keyMsg.getId());
    }

    @Override
    public byte[] write(TransactionKey key) throws IOException {
        IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
        keyBuilder.setId(key.getId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        keyBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}