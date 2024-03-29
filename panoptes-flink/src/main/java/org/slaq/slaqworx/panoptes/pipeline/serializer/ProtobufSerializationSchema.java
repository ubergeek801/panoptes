package org.slaq.slaqworx.panoptes.pipeline.serializer;

import io.micronaut.context.BeanProvider;
import java.io.Serial;
import javax.annotation.Nonnull;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.pipeline.PanoptesApp;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A convenient base class for implementing {@link KafkaSerializationSchema}e that delegate to a
 * {@link ProtobufSerializer}.
 *
 * @param <T> the type to be serialized
 * @author jeremy
 */
public abstract class ProtobufSerializationSchema<T extends ProtobufSerializable>
    implements KafkaRecordSerializationSchema<T>,
        BeanProvider<AssetCache>,
        PortfolioProvider,
        SecurityProvider {
  @Serial private static final long serialVersionUID = 1L;

  private final String topic;

  private transient AssetCache assetCache;

  private transient ProtobufSerializer<T> serializer;

  /**
   * Creates a new {@link ProtobufSerializationSchema} for the given topic.
   *
   * @param topic the topic to which this serialization schema is to be applied
   */
  protected ProtobufSerializationSchema(String topic) {
    this.topic = topic;
  }

  @Override
  @Nonnull
  public final AssetCache get() {
    if (assetCache == null) {
      assetCache = PanoptesApp.getApplicationContext().getBean(AssetCache.class);
    }

    return assetCache;
  }

  @Override
  public Portfolio getPortfolio(@Nonnull PortfolioKey key) {
    return get().getPortfolio(key);
  }

  @Override
  public Security getSecurity(@Nonnull SecurityKey key, @Nonnull EvaluationContext context) {
    return get().getSecurity(key, context);
  }

  @Override
  public ProducerRecord<byte[], byte[]> serialize(
      T element, KafkaSinkContext context, Long timestamp) {
    try {
      byte[] serializedKey = serializeKey(element);
      byte[] serializedElement = getSerializer().write(element);

      return new ProducerRecord<>(topic, serializedKey, serializedElement);
    } catch (Exception e) {
      // TODO throw a better exception
      throw new RuntimeException("could not serialize object", e);
    }
  }

  /**
   * Creates a {@link ProtobufSerializer} that can handle this {@link ProtobufSerializationSchema}'s
   * associated type.
   *
   * @return a {@link ProtobufSerializer}
   */
  protected abstract ProtobufSerializer<T> createSerializer();

  /**
   * Obtains (creating, if necessary) a {@link ProtobufSerializer} that can handle this {@link
   * ProtobufSerializationSchema}'s associated type.
   *
   * @return a {@link ProtobufSerializer}
   */
  protected final ProtobufSerializer<T> getSerializer() {
    if (serializer == null) {
      serializer = createSerializer();
    }

    return serializer;
  }

  /**
   * Produces a serialized representation of the given element's key.
   *
   * @param element the element for which to provide a serialized key
   * @return a serialized representation of the element's key, or {@code null} if not applicable
   * @throws Exception if the key could not be serialized
   */
  protected abstract byte[] serializeKey(T element) throws Exception;
}
