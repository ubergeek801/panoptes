package org.slaq.slaqworx.panoptes.serializer;

import com.hazelcast.nio.serialization.ByteArraySerializer;

/**
 * A {@code ByteArraySerializer} which enforces that serializable types are marked with the
 * {@code ProtobufSerializable} interface.
 *
 * @author jeremy
 * @param <T>
 *            the type of serialized object
 */
public interface ProtobufSerializer<T extends ProtobufSerializable> extends ByteArraySerializer<T> {
    // trivial inheritance
}
