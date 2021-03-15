package org.slaq.slaqworx.panoptes.cache;

import com.hazelcast.config.Config;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

/**
 * A Micronaut {@link Factory} that provides {@link Bean}s related to the Hazelcast cache, suitable
 * for unit testing {@code panoptes-common}.
 *
 * @author jeremy
 */
@Factory
public class PanoptesCacheTestConfiguration {
  /**
   * Creates a new {@link PanoptesCacheTestConfiguration}. Restricted because instances of this
   * class should be obtained through the {@link ApplicationContext} (if it is needed at all).
   */
  protected PanoptesCacheTestConfiguration() {
    // nothing to do
  }

  /**
   * Provides a {@link HazelcastInstance} suitable for a unit test environment.
   *
   * @param serializationConfig
   *     the {@link SerializationConfig} to use for the cache
   *
   * @return a {@link HazelcastInstance}
   */
  @Bean(preDestroy = "shutdown")
  @Singleton
  protected HazelcastInstance hazelcastInstance(SerializationConfig serializationConfig) {
    Config config = new Config("panoptes-test");
    config.setProperty("hazelcast.logging.type", "slf4j");
    config.setSerializationConfig(serializationConfig);
    config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

    return Hazelcast.getOrCreateHazelcastInstance(config);
  }
}
