package org.slaq.slaqworx.panoptes.serializer;

import com.hazelcast.nio.serialization.ByteArraySerializer;

/**
 * {@code ProtobufSerializer} is nothing more than a {@code ByteArraySerializer} that enforces that
 * serializable types are marked with the {@code PortobufSerializable} interface.
 *
 * @author jeremy
 * @param <T>
 *            the type of serialized object
 */
public interface ProtobufSerializer<T extends ProtobufSerializable> extends ByteArraySerializer<T> {
    // trivial inheritance
}
