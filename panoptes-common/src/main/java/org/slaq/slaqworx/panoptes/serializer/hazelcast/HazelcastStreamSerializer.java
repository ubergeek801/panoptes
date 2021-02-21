package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A convenient base class to implement Hazelcast {@code StreamSerializer}s which delegate to
 * existing Protobuf serializers.
 *
 * @author jeremy
 * @param <T>
 *            the type being (de)serialized
 */
public abstract class HazelcastStreamSerializer<T extends ProtobufSerializable>
        implements StreamSerializer<T> {
    /**
     * Enumerates the type IDs used by Hazelcast serializers.
     *
     * @author jeremy
     */
    public enum SerializerTypeId {
        DONT_USE_ZERO,
        POSITION_KEY,
        POSITION,
        PORTFOLIO_KEY,
        PORTFOLIO_RULE_KEY,
        PORTFOLIO,
        PORTFOLIO_EVENT,
        PORTFOLIO_SUMMARIZER,
        PORTFOLIO_SUMMARY,
        SECURITY_KEY,
        SECURITY,
        RULE_KEY,
        RULE,
        RULE_SUMMARY,
        TRANSACTION_KEY,
        TRANSACTION,
        TRADE_KEY,
        TRADE,
        EVALUATION_CONTEXT,
        PORTFOLIO_EVALUATION_REQUEST,
        ROOM_EVALUATION_REQUEST,
        TRADE_EVALUATION_REQUEST,
        EXCEPTION,
        RULE_RESULT,
        EVALUATION_RESULT,
        RULE_IMPACT,
        PORTFOLIO_RULE_IMPACT,
        TRADE_EVALUATION_RESULT,
        RULE_EVALUATION_RESULT
    }

    private final ProtobufSerializer<T> protobufSerializer;

    /**
     * Creates a new {@code HazelcastStreamSerializer} which delegates to the given
     * {@code ProtobufSerializer}.
     *
     * @param protobufSerializer
     *            the {@code ProtobufSerializer} to which to delegate
     */
    protected HazelcastStreamSerializer(ProtobufSerializer<T> protobufSerializer) {
        this.protobufSerializer = protobufSerializer;
    }

    @Override
    public T read(ObjectDataInput in) throws IOException {
        int length = in.readInt();
        byte[] buffer = new byte[length];
        in.readFully(buffer);

        return protobufSerializer.read(buffer);
    }

    @Override
    public void write(ObjectDataOutput out, T object) throws IOException {
        byte[] buffer = protobufSerializer.write(object);
        // when deserializing, ObjectDataInput won't know the size of the byte array unless we tell
        // it
        out.writeInt(buffer.length);
        out.write(buffer);
    }
}
