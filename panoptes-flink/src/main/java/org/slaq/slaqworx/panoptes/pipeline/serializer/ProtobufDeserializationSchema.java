package org.slaq.slaqworx.panoptes.pipeline.serializer;

import javax.inject.Provider;

import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.pipeline.PanoptesApp;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A convenient base class for implementing {@code KafkaDeserializationSchema}e that delegate to a
 * {@code ProtobufSerializer}.
 *
 * @author jeremy
 * @param <T>
 *            the type to be deserialized
 */
public abstract class ProtobufDeserializationSchema<T extends ProtobufSerializable>
        implements KafkaDeserializationSchema<T>, Provider<AssetCache> {
    private static final long serialVersionUID = 1L;

    private transient AssetCache assetCache;
    private transient ProtobufSerializer<T> serializer;

    /**
     * Creates a new {@code ProtobufDeserializationSchema}.
     */
    protected ProtobufDeserializationSchema() {
        // nothing to do
    }

    @Override
    public T deserialize(ConsumerRecord<byte[], byte[]> record) throws Exception {
        return getSerializer().read(record.value());
    }

    @Override
    public final AssetCache get() {
        if (assetCache == null) {
            assetCache = PanoptesApp.getApplicationContext().getBean(AssetCache.class);
        }

        return assetCache;
    }

    @Override
    public boolean isEndOfStream(T nextElement) {
        return false;
    }

    /**
     * Creates a {@code ProtobufSerializer} instance appropriate for the handled type.
     *
     * @return a {@code ProtobufSerializer}
     */
    protected abstract ProtobufSerializer<T> createSerializer();

    /**
     * Obtains the singleton {@code ProtobufSerializer}, creating it if necessary using
     * {@code createProtobufSerializer()}.
     *
     * @return a {@code ProtobufSerializer}
     */
    protected final ProtobufSerializer<T> getSerializer() {
        if (serializer == null) {
            serializer = createSerializer();
        }

        return serializer;
    }
}
