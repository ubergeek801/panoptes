package org.slaq.slaqworx.panoptes.pipeline.serializer;

import javax.inject.Provider;

import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.pipeline.PanoptesApp;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

public abstract class ProtobufDeserializationSchema<T extends ProtobufSerializable>
        implements KafkaDeserializationSchema<T>, Provider<AssetCache> {
    private static final long serialVersionUID = 1L;

    private transient AssetCache assetCache;
    private transient ProtobufSerializer<T> serializer;

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

    protected abstract ProtobufSerializer<T> createSerializer();

    protected final ProtobufSerializer<T> getSerializer() {
        if (serializer == null) {
            serializer = createSerializer();
        }

        return serializer;
    }
}
