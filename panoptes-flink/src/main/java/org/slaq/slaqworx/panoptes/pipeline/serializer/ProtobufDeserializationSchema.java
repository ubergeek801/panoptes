package org.slaq.slaqworx.panoptes.pipeline.serializer;

import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A convenient base class for implementing {@link KafkaDeserializationSchema}e that delegate to a
 * {@link ProtobufSerializer}.
 *
 * @param <T> the type to be deserialized
 * @author jeremy
 */
public abstract class ProtobufDeserializationSchema<T extends ProtobufSerializable>
    implements KafkaDeserializationSchema<T> {
  private static final long serialVersionUID = 1L;

  private transient ProtobufSerializer<T> serializer;

  /** Creates a new {@link ProtobufDeserializationSchema}. */
  protected ProtobufDeserializationSchema() {
    // nothing to do
  }

  @Override
  public T deserialize(ConsumerRecord<byte[], byte[]> record) throws Exception {
    return getSerializer().read(record.value());
  }

  @Override
  public boolean isEndOfStream(T nextElement) {
    return false;
  }

  /**
   * Creates a {@link ProtobufSerializer} instance appropriate for the handled type.
   *
   * @return a {@link ProtobufSerializer}
   */
  protected abstract ProtobufSerializer<T> createSerializer();

  /**
   * Obtains the singleton {@link ProtobufSerializer}, creating it if necessary using {@link
   * #createSerializer()}.
   *
   * @return a {@link ProtobufSerializer}
   */
  protected final ProtobufSerializer<T> getSerializer() {
    if (serializer == null) {
      serializer = createSerializer();
    }

    return serializer;
  }
}
