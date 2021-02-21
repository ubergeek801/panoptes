package org.slaq.slaqworx.panoptes.serializer;

import java.io.IOException;

/**
 * A simple (de)serializer interface which enforces that serializable types are marked with the
 * {@code ProtobufSerializable} interface.
 *
 * @author jeremy
 * @param <T>
 *            the type of serialized object
 */
public interface ProtobufSerializer<T extends ProtobufSerializable> {
    /**
     * Deserializes an object from the given byte array.
     *
     * @param buffer
     *            a byte array from which to deserialize
     * @return a deserialized object
     * @throws IOException
     *             if an object could not be deserialized
     */
    public T read(byte[] buffer) throws IOException;

    /**
     * Serializes the given object into a byte array.
     *
     * @param object
     *            the object to serialize
     * @return a byte array containing the serialized contents
     * @throws IOException
     *             if the object could not be serialized
     */
    public byte[] write(T object) throws IOException;
}
