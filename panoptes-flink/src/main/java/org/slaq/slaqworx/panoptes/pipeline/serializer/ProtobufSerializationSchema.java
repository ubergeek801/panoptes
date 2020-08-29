package org.slaq.slaqworx.panoptes.pipeline.serializer;

import javax.inject.Provider;

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

public abstract class ProtobufSerializationSchema<T extends ProtobufSerializable> implements
        KafkaSerializationSchema<T>, Provider<AssetCache>, PortfolioProvider, SecurityProvider {
    private static final long serialVersionUID = 1L;

    private final String topic;

    private transient AssetCache assetCache;

    private transient ProtobufSerializer<T> serializer;

    protected ProtobufSerializationSchema(String topic) {
        this.topic = topic;
    }

    @Override
    public final AssetCache get() {
        if (assetCache == null) {
            assetCache = PanoptesApp.getApplicationContext().getBean(AssetCache.class);
        }

        return assetCache;
    }

    @Override
    public Portfolio getPortfolio(PortfolioKey key) {
        return get().getPortfolio(key);
    }

    @Override
    public Security getSecurity(SecurityKey key, EvaluationContext context) {
        return get().getSecurity(key, context);
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(T element, Long timestamp) {
        try {
            byte[] serializedKey = serializeKey(element);
            byte[] serializedElement = serializer.write(element);

            return new ProducerRecord<>(topic, serializedKey, serializedElement);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not serialize object", e);
        }
    }

    protected abstract ProtobufSerializer<T> createSerializer();

    protected final ProtobufSerializer<T> getSerializer() {
        if (serializer == null) {
            serializer = createSerializer();
        }

        return serializer;
    }

    protected byte[] serializeKey(T element) throws Exception {
        return null;
    }
}
