package org.slaq.slaqworx.panoptes.data;

import javax.inject.Named;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.CoreQueueConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.ProxyFactory;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.rule.MaterializedRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.serializer.PortfolioKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioSerializer;
import org.slaq.slaqworx.panoptes.serializer.PositionKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.PositionSerializer;
import org.slaq.slaqworx.panoptes.serializer.RuleKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.RuleSerializer;
import org.slaq.slaqworx.panoptes.serializer.SecurityKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.SecuritySerializer;

/**
 * {@code PanoptesCacheConfiguration} is a Micronaut {@code Factory} that provides {@code Bean}s
 * related to the Hazelcast cache.
 *
 * @author jeremy
 */
@Factory
public class PanoptesCacheConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(PanoptesCacheConfiguration.class);

    private static final Object activeMQBrokerMutex = new Object();
    private static EmbeddedActiveMQ activeMQServer;

    /**
     * Creates a new {@code PanoptesCacheConfiguration}. Restricted because instances of this class
     * should be obtained through the {@code ApplicationContext} (if it is needed at all).
     */
    protected PanoptesCacheConfiguration() {
        // nothing to do
    }

    /**
     * Provides a {@code MapConfig} for the specified map and adds it to the given Hazelcast
     * {@code Config}.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to use to create the {@code MapStore}
     * @param cacheName
     *            the name of the map/cache being created
     * @return a {@code MapConfig} configured for the given map
     */
    protected MapConfig createMapConfiguration(HazelcastMapStoreFactory mapStoreFactory,
            String cacheName) {
        MapStoreConfig mapStoreConfig = new MapStoreConfig()
                .setFactoryImplementation(mapStoreFactory).setWriteDelaySeconds(15)
                .setWriteBatchSize(1000).setInitialLoadMode(InitialLoadMode.LAZY);
        NearCacheConfig nearCacheConfig = new NearCacheConfig().setName("near-" + cacheName)
                .setInMemoryFormat(InMemoryFormat.OBJECT).setCacheLocalEntries(true);
        MapConfig mapConfig = new MapConfig(cacheName).setBackupCount(3).setReadBackupData(true)
                .setInMemoryFormat(InMemoryFormat.BINARY).setMapStoreConfig(mapStoreConfig)
                .setNearCacheConfig(nearCacheConfig);

        return mapConfig;
    }

    protected void createMessagingService(boolean isUseTcp) throws Exception {
        synchronized (activeMQBrokerMutex) {
            if (activeMQServer == null) {
                LOG.info("creating messaging service on local node");

                Configuration config = new ConfigurationImpl();

                config.addAcceptorConfiguration("in-vm", "vm://0");
                if (isUseTcp) {
                    config.addAcceptorConfiguration("tcp", "tcp://0.0.0.0:61616");
                }
                config.setPersistenceEnabled(false);
                config.setSecurityEnabled(false);

                CoreQueueConfiguration portfolioEvaluationRequestQueueConfig =
                        new CoreQueueConfiguration()
                                .setAddress(PortfolioCache.PORTFOLIO_EVALUATION_REQUEST_QUEUE_NAME)
                                .setName(PortfolioCache.PORTFOLIO_EVALUATION_REQUEST_QUEUE_NAME)
                                .setRoutingType(RoutingType.ANYCAST);
                config.addQueueConfiguration(portfolioEvaluationRequestQueueConfig);

                activeMQServer = new EmbeddedActiveMQ();
                activeMQServer.setConfiguration(config);
                activeMQServer.start();
            }
        }
    }

    /**
     * Provides a Hazelcast configuration suitable for the detected runtime environment.
     *
     * @param securityAttributeLoader
     *            the {@SecurityAttributeLoader} used to initialize {@code SecurityAttribute}s
     * @param hazelcastMapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to be used to create the
     *            {@code MapStoreConfig}s
     * @return a Hazelcast {@code Config}
     */
    @Bean
    protected Config hazelcastConfig(SecurityAttributeLoader securityAttributeLoader,
            HazelcastMapStoreFactory hazelcastMapStoreFactory,
            @Named("portfolio") MapConfig portfolioMapConfig,
            @Named("position") MapConfig positionMapConfig,
            @Named("security") MapConfig securityMapConfig,
            @Named("rule") MapConfig ruleMapConfig) {
        securityAttributeLoader.loadSecurityAttributes();

        boolean isClustered = (System.getenv("KUBERNETES_SERVICE_HOST") != null);

        Config config = new Config("panoptes");
        config.setProperty("hazelcast.logging.type", "slf4j");
        // this probably isn't good for fault tolerance but it improves startup time
        if (isClustered) {
            config.setProperty("hazelcast.initial.min.cluster.size", "4");
        }

        // set up the entity caches (Portfolio, Position, etc.)

        SerializationConfig serializationConfig = config.getSerializationConfig();
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(PortfolioKeySerializer.class).setTypeClass(PortfolioKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(PortfolioSerializer.class).setTypeClass(Portfolio.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(PositionKeySerializer.class).setTypeClass(PositionKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(PositionSerializer.class).setTypeClass(MaterializedPosition.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(RuleKeySerializer.class).setTypeClass(RuleKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(RuleSerializer.class).setTypeClass(MaterializedRule.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(SecurityKeySerializer.class).setTypeClass(SecurityKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setClass(SecuritySerializer.class).setTypeClass(Security.class));

        config.addMapConfig(portfolioMapConfig).addMapConfig(positionMapConfig)
                .addMapConfig(securityMapConfig).addMapConfig(ruleMapConfig);

        // set up a map to act as the portfolio evaluation result "topic"
        config.getMapConfig(PortfolioCache.PORTFOLIO_EVALUATION_RESULT_MAP_NAME).setBackupCount(0)
                .setInMemoryFormat(InMemoryFormat.BINARY);

        // set up cluster join discovery appropriate for the detected environment
        if (isClustered) {
            // use Kubernetes discovery
            // TODO parameterize the cluster DNS property
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                    .setProperty("service-dns", "panoptes-hazelcast.default.svc.cluster.local");
        } else {
            // not running in Kubernetes; run standalone
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        }

        return config;
    }

    /**
     * Provides a {@code HazelcastInstance} configured with the given configuration.
     *
     * @param hazelcastConfiguration
     *            the Hazelcast {@Config} with which to configure the instance
     * @return a {@HazelcastInstance}
     */
    @Bean
    protected HazelcastInstance hazelcastInstance(Config hazelcastConfiguration) {
        return Hazelcast.newHazelcastInstance(hazelcastConfiguration);
    }

    @Bean
    protected ClientSessionFactory messagingClientSessionFactory(ServerLocator serverLocator)
            throws Exception {
        return serverLocator.createSessionFactory();
    }

    @Bean
    @Requires(notEnv = "standalone")
    @SuppressWarnings("resource")
    protected ServerLocator messagingServerLocator() throws Exception {
        ServerLocator locator;
        // TODO make ActiveMQ broker configuration a little more portable
        if ("uberkube02".equals(System.getenv("NODENAME"))) {
            createMessagingService(true);
            locator = ActiveMQClient.createServerLocator("vm://0");
        } else {
            locator = ActiveMQClient.createServerLocator("tcp://uberkube02:61616")
                    .setInitialConnectAttempts(10);
        }
        // don't prefetch too much, or work will pile up unevenly on busier nodes (note that this
        // number is in bytes, not messages)
        locator.setConsumerWindowSize(2048);

        return locator;
    }

    @Bean
    @Requires(env = "standalone")
    protected ServerLocator messagingServerLocatorStandalone() throws Exception {
        createMessagingService(false);

        return ActiveMQClient.createServerLocator("vm://0");
    }

    @Bean
    @SuppressWarnings("resource")
    protected ClientConsumer portfolioEvaluationRequestConsumer(ClientSessionFactory sessionFactory)
            throws ActiveMQException {
        ClientSession session = sessionFactory.createSession();
        ClientConsumer consumer =
                session.createConsumer(PortfolioCache.PORTFOLIO_EVALUATION_REQUEST_QUEUE_NAME);
        session.start();

        return consumer;
    }

    @Bean
    @SuppressWarnings("resource")
    protected Pair<ClientSession, ClientProducer> portfolioEvaluationRequestProducer(
            ClientSessionFactory sessionFactory) throws ActiveMQException {
        ClientSession session = sessionFactory.createSession();
        return new ImmutablePair<>(session,
                session.createProducer(PortfolioCache.PORTFOLIO_EVALUATION_REQUEST_QUEUE_NAME));
    }

    /**
     * Provides a {@code MapConfig} for the {@code Portfolio} cache.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration
     * @return a {@code MapConfig}
     */
    @Named("portfolio")
    @Bean
    protected MapConfig portfolioMapStoreConfig(HazelcastMapStoreFactory mapStoreFactory) {
        return createMapConfiguration(mapStoreFactory, PortfolioCache.PORTFOLIO_CACHE_NAME);
    }

    /**
     * Provides a {@code MapConfig} for the {@code Position} cache.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration
     * @return a {@code MapConfig}
     */
    @Named("position")
    @Bean
    protected MapConfig positionMapStoreConfig(HazelcastMapStoreFactory mapStoreFactory) {
        return createMapConfiguration(mapStoreFactory, PortfolioCache.POSITION_CACHE_NAME);
    }

    /**
     * Provides a {@code ProxyFactory} which uses the specified {@code ApplicationContext} to
     * resolve references.
     *
     * @param applicationContext
     *            the {@code ApplicationContext} to use when resolving references
     * @return a {@code ProxyFactory}
     */
    @Bean
    protected ProxyFactory proxyFactory(ApplicationContext applicationContext) {
        return new ProxyFactory(applicationContext);
    }

    /**
     * Provides a {@code MapConfig} for the {@code Rule} cache.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration
     * @return a {@code MapConfig}
     */
    @Named("rule")
    @Bean
    protected MapConfig ruleMapStoreConfig(HazelcastMapStoreFactory mapStoreFactory) {
        return createMapConfiguration(mapStoreFactory, PortfolioCache.RULE_CACHE_NAME);
    }

    /**
     * Provides a {@code MapConfig} for the {@code Security} cache.
     *
     * @param mapStoreFactory
     *            the {@code HazelcastMapStoreFactory} to provide to the configuration
     * @return a {@code MapConfig}
     */
    @Named("security")
    @Bean
    protected MapConfig securityMapStoreConfig(HazelcastMapStoreFactory mapStoreFactory) {
        return createMapConfiguration(mapStoreFactory, PortfolioCache.SECURITY_CACHE_NAME);
    }
}
