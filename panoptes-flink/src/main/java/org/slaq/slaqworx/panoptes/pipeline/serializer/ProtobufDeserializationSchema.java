package org.slaq.slaqworx.panoptes.pipeline.serializer;

import java.io.IOException;
import java.io.Serial;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A convenient base class for implementing {@link KafkaRecordDeserializationSchema}e that delegate
 * to a {@link ProtobufSerializer}.
 *
 * @param <T> the type to be deserialized
 * @author jeremy
 */
public abstract class ProtobufDeserializationSchema<T extends ProtobufSerializable>
    implements KafkaRecordDeserializationSchema<T> {
  @Serial private static final long serialVersionUID = 1L;

  private transient ProtobufSerializer<T> serializer;

  /** Creates a new {@link ProtobufDeserializationSchema}. */
  protected ProtobufDeserializationSchema() {
    // nothing to do
  }

  @Override
  public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<T> out)
      throws IOException {
    T deserialized = getSerializer().read(record.value());
    out.collect(deserialized);
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
