package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.IOException;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A convenient base class for implementing a Kryo {@link Serializer} (for Flink compatibility)
 * using Protobuf.
 *
 * @param <T>
 *     the type to be serialized
 *
 * @author jeremy
 */
public abstract class ProtobufKryoSerializer<T extends ProtobufSerializable> extends Serializer<T> {
  private ProtobufSerializer<T> serializer;

  /**
   * Creates a new {@link ProtobufKryoSerializer}.
   */
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

  /**
   * Creates a {@link ProtobufSerializer} instance appropriate for the handled type.
   *
   * @return a {@link ProtobufSerializer}
   */
  protected abstract ProtobufSerializer<T> createProtobufSerializer();

  /**
   * Obtains the singleton {@link ProtobufSerializer}, creating it if necessary using {@link
   * #createProtobufSerializer()}.
   *
   * @return a {@link ProtobufSerializer}
   */
  protected final ProtobufSerializer<T> getProtobufSerializer() {
    if (serializer == null) {
      serializer = createProtobufSerializer();
    }

    return serializer;
  }
}
