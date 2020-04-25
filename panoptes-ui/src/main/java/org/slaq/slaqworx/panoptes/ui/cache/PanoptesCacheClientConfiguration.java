package org.slaq.slaqworx.panoptes.ui.cache;

import javax.inject.Singleton;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.HazelcastInstance;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

@Factory
public class PanoptesCacheClientConfiguration {
    /**
     * Provides a {@code HazelcastInstance} suitable for a cache client environment.
     *
     * @param serializationConfig
     *            the {@code SerializationConfig} to use for the cache
     * @return a {@code HazelcastInstance}
     */
    @Bean(preDestroy = "shutdown")
    @Singleton
    protected HazelcastInstance hazelcastInstance(SerializationConfig serializationConfig) {
        ClientConfig config = new ClientConfig();
        config.setProperty("hazelcast.logging.type", "slf4j");
        config.setClusterName("panoptes");
        config.setSerializationConfig(serializationConfig);
        // FIXME make this configurable
        // config.getNetworkConfig().addAddress("localhost:5701");
        config.getNetworkConfig().addAddress("10.13.35.82:5701");

        return HazelcastClient.newHazelcastClient(config);
    }
}
