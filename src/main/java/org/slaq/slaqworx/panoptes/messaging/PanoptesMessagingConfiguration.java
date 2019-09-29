package org.slaq.slaqworx.panoptes.messaging;

import javax.inject.Singleton;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.CoreQueueConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code PanoptesMessagingConfiguration} is a Micronaut {@code Factory} that provides {@code Bean}s
 * related to Artemis messaging.
 *
 * @author jeremy
 */
@Factory
public class PanoptesMessagingConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(PanoptesMessagingConfiguration.class);

    private static final Object activeMQBrokerMutex = new Object();
    private static EmbeddedActiveMQ activeMQServer;

    protected static final String PORTFOLIO_EVALUATION_REQUEST_QUEUE_NAME =
            "portfolioEvaluationRequestQueue";

    /**
     * Creates a new {@code PanoptesMessagingConfiguration}. Restricted because instances of this
     * class should be obtained through the {@code ApplicationContext} (if it is needed at all).
     */
    protected PanoptesMessagingConfiguration() {
        // nothing to do
    }

    /**
     * Creates and starts a new Artemis messaging service, as a singleton referenced through the
     * {@code activeMQServer} field.
     *
     * @param isUseTcp
     *            {@code true} if a TCP acceptor is to be created; {@code false} to create an in-vm
     *            acceptor only
     * @throws Exception
     *             if the service could not be created
     */
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
                                .setAddress(PORTFOLIO_EVALUATION_REQUEST_QUEUE_NAME)
                                .setName(PORTFOLIO_EVALUATION_REQUEST_QUEUE_NAME)
                                .setRoutingType(RoutingType.ANYCAST).setDurable(false);
                config.addQueueConfiguration(portfolioEvaluationRequestQueueConfig);

                activeMQServer = new EmbeddedActiveMQ();
                activeMQServer.setConfiguration(config);
                activeMQServer.start();
            }
        }
    }

    /**
     * Provides a {@code ClientSessionFactory} suitable for accessing Artemis resources.
     *
     * @param serverLocator
     *            the {@code ServerLocator} to use to create a session factory
     * @return a {@code ClientSessionFactory}
     * @throws Exception
     *             if the factory could not be created
     */
    @Bean
    protected ClientSessionFactory messagingClientSessionFactory(ServerLocator serverLocator)
            throws Exception {
        return serverLocator.createSessionFactory();
    }

    /**
     * Provides a {@code ServerLocator} suitable for accessing Artemis resources, either remotely or
     * in-VM depending on the detected environment.
     *
     * @return a {@code ServerLocator}
     * @throws Exception
     *             if the factory could not be created
     */
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
        // don't prefetch too much, or work will pile up unevenly on busier nodes (note that the
        // unit is bytes, not messages)
        locator.setConsumerWindowSize(-1);
        locator.setProducerWindowSize(-1);
        locator.setPreAcknowledge(true);
        locator.setBlockOnAcknowledge(false);
        locator.setBlockOnNonDurableSend(false);

        return locator;
    }

    /**
     * Provides a {@code ServerLocator} suitable for accessing in-VM Artemis resources.
     *
     * @return a {@code ServerLocator} for the local VM
     * @throws Exception
     *             if the factory could not be created
     */
    @Bean
    @Requires(env = "standalone")
    protected ServerLocator messagingServerLocatorStandalone() throws Exception {
        createMessagingService(false);

        return ActiveMQClient.createServerLocator("vm://0");
    }

    /**
     * Provides a {@code ClientConsumer} suitable for consuming messages from the {@code Portfolio}
     * evaluation queue.
     *
     * @param sessionFactory
     *            the {@code ClientSessionFactory} to use to create a consumer
     * @return a {@code ClientConsumer}
     * @throws ActiveMQException
     *             if the consumer could not be created
     */
    @Bean
    @SuppressWarnings("resource")
    protected ClientConsumer portfolioEvaluationRequestConsumer(ClientSessionFactory sessionFactory)
            throws ActiveMQException {
        ClientSession session = sessionFactory.createSession();
        ClientConsumer consumer = session.createConsumer(PORTFOLIO_EVALUATION_REQUEST_QUEUE_NAME);
        session.start();

        return consumer;
    }

    /**
     * Provides a {@code ClientProducerSessionFactory} which provides a {@code ClientSession} and
     * {@code ClientProducer} suitable for producing messages to the {@code Portfolio} evaluation
     * queue.
     *
     * @param sessionFactory
     *            the {@code ClientSessionFactory} to use to create a producer
     * @return a {@code ClientProducerSessionFactory} providing a {@code ClientSession} and
     *         {@code ClientProducer}
     */
    @Singleton
    protected ClientProducerSessionFactory
            portfolioEvaluationRequestProducerSessionFactory(ClientSessionFactory sessionFactory) {
        return new ClientProducerSessionFactory(sessionFactory,
                PORTFOLIO_EVALUATION_REQUEST_QUEUE_NAME);
    }
}
