package org.slaq.slaqworx.panoptes;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapStore;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.data.PortfolioCache;

/**
 * Panoptes is a prototype system for investment portfolio compliance assurance.
 *
 * @author jeremy
 */
@SpringBootApplication
public class Panoptes {
    private static final Logger LOG = LoggerFactory.getLogger(Panoptes.class);

    /**
     * The entry point for the Panoptes application. Currently the app doesn't do anything useful.
     *
     * @param args
     *            the program arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Panoptes.class, args);
    }

    /**
     * Provides a Hazelcast configuration suitable for the detected runtime environment. Spring Boot
     * will automatically use this configuration when it creates the HazelcastInstance.
     *
     * @return a Hazelcast Config
     * @throws Exception
     *             if the configuration could not be created
     */
    @Bean
    public Config hazelcastConfig() throws Exception {
        Config config = new Config();

        for (Entry<String, Class<? extends MapStore<?, ?>>> mapStoreEntry : PortfolioCache
                .getMapStores().entrySet()) {
            String name = mapStoreEntry.getKey();
            Class<? extends MapStore<?, ?>> loaderClass = mapStoreEntry.getValue();
            MapStoreConfig mapStoreConfig = new MapStoreConfig()
                    .setImplementation(loaderClass.getDeclaredConstructor().newInstance());
            NearCacheConfig nearCacheConfig =
                    new NearCacheConfig().setInMemoryFormat(InMemoryFormat.BINARY);
            config.getMapConfig(name).setBackupCount(0).setInMemoryFormat(InMemoryFormat.BINARY)
                    .setMapStoreConfig(mapStoreConfig).setNearCacheConfig(nearCacheConfig);
        }

        if (System.getenv("KUBERNETES_SERVICE_HOST") == null) {
            // not running in Kubernetes; run standalone
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        } else {
            // use Kubernetes discovery
            // FIXME parameterize the cluster DNS property
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                    .setProperty("service-dns", "panoptes-hazelcast.default.svc.cluster.local");
        }

        return config;
    }

    /**
     * Provides an ApplicationRunner to be executed upon Panoptes startup.
     *
     * @param appContext
     *            the Spring ApplicationContext
     * @return an ApplicationRunner which initializes Panoptes
     */
    @Bean
    public ApplicationRunner startupRunner(ApplicationContext appContext) {
        return args -> {
            HazelcastInstance hazelcastInstance = appContext.getBean(HazelcastInstance.class);

            Map<String, Portfolio> portfolios = hazelcastInstance.getMap("portfolio");
            LOG.info("got {} Portfolios from cache", portfolios.size());
        };
    }
}
