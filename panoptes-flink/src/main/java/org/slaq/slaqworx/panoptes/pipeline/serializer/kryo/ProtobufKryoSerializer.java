package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

public abstract class ProtobufKryoSerializer<T extends ProtobufSerializable> extends Serializer<T> {
    private ProtobufSerializer<T> serializer;

    protected ProtobufKryoSerializer() {
        // nothing to do
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> type) {
        try {
            int length = input.readInt(true);
            byte[] buffer = new byte[length];
            input.readBytes(buffer);
            return getProtobufSerializer().read(buffer);
        } catch (IOException e) {
            // FIXME throw a real exception
            throw new RuntimeException("could not deserialize input", e);
        }
    }

    @Override
    public void write(Kryo kryo, Output output, T object) {
        try {
            byte[] buffer = getProtobufSerializer().write(object);
            output.writeInt(buffer.length, true);
            output.writeBytes(buffer);
        } catch (IOException e) {
            // FIXME throw a real exception
            throw new RuntimeException("could not deserialize input", e);
        }
    }

    protected abstract ProtobufSerializer<T> createProtobufSerializer();

    protected final ProtobufSerializer<T> getProtobufSerializer() {
        if (serializer == null) {
            serializer = createProtobufSerializer();
        }

        return serializer;
    }
}
