package org.slaq.slaqworx.panoptes;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.HazelcastInstance;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.data.DummyPortfolioMapLoader;

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
     * Provides a Hazelcast configuration suitable for the detected runtime environment.
     *
     * @return a Hazelcast Config
     * @throws IOException
     *             if the configuration could not be created
     */
    @Bean
    public Config hazelcastConfig() throws IOException {
        Config config = new Config();

        MapStoreConfig portfolioMapStoreConfig = new MapStoreConfig();
        portfolioMapStoreConfig.setImplementation(new DummyPortfolioMapLoader());
        config.getMapConfig("portfolios").setBackupCount(0)
                .setMapStoreConfig(portfolioMapStoreConfig);

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
            // the HazelcastInstance exists courtesy of Spring Boot integration
            HazelcastInstance hazelcastInstance = appContext.getBean(HazelcastInstance.class);

            Map<String, Portfolio> portfolios = hazelcastInstance.getMap("portfolios");
            LOG.info("got {} Portfolios from cache", portfolios.size());
        };
    }
}
