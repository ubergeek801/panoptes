package org.slaq.slaqworx.panoptes.serializer.kafka;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A convenient base class to adapt a {@code ProtobufSerializer} to a Kafka {@code Deserializer} and
 * {@code Serializer}.
 *
 * @author jeremy
 * @param <T>
 *            the type to be (de)serialized
 */
public abstract class KafkaSerializer<T extends ProtobufSerializable>
        implements Deserializer<T>, Serializer<T> {
    private final ProtobufSerializer<T> protobufSerializer;

    /**
     * Creates a new {@code KafkaSerializer} which delegates to the given
     * {@code ProtobufSerializer}.
     *
     * @param protobufSerializer
     *            the {@code ProtobufSerializer} to which to delegate
     */
    protected KafkaSerializer(ProtobufSerializer<T> protobufSerializer) {
        this.protobufSerializer = protobufSerializer;
    }

    @Override
    public void close() {
        // both Serializer and Deserializer provide empty default implementations, so just pick one
        // to override
        Deserializer.super.close();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // both Serializer and Deserializer provide empty default implementations, so just pick one
        // to override
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return protobufSerializer.read(data);
        } catch (IOException e) {
            // FIXME throw a better exception
            throw new RuntimeException("could not deserialize data for topic " + topic, e);
        }
    }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return protobufSerializer.write(data);
        } catch (IOException e) {
            // FIXME throw a better exception
            throw new RuntimeException("could not serialize data for topic " + topic, e);
        }
    }
}
