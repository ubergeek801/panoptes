package org.slaq.slaqworx.panoptes.messaging;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;

/**
 * {@code ClientProducerSessionFactory} provides {@code ThreadLocal}-based access to a
 * {@code ClientProducer} and a {@code ClientSession}, since producing messages typically requires
 * both.
 *
 * @author jeremy
 */
public class ClientProducerSessionFactory {
    private final ThreadLocal<ClientSession> session;
    private final ThreadLocal<ClientProducer> producer;

    /**
     * Creates a new {@code ClientProducerSessionFactory} which delegates to the given
     * {@code ClientSessionFactory}.
     *
     * @param sessionFactory
     *            the {@code ClientSessionFactory} to which to delegate
     * @param destinationName
     *            the destination (e.g. queue) name for which to produce messages
     */
    protected ClientProducerSessionFactory(ClientSessionFactory sessionFactory,
            String destinationName) {
        session = new ThreadLocal<>() {
            @Override
            protected ClientSession initialValue() {
                try {
                    return sessionFactory.createSession();
                } catch (ActiveMQException e) {
                    // TODO throw a real exception
                    throw new RuntimeException("could not create session for " + destinationName,
                            e);
                }
            }
        };
        producer = new ThreadLocal<>() {
            @Override
            protected ClientProducer initialValue() {
                try {
                    return session.get().createProducer(destinationName);
                } catch (ActiveMQException e) {
                    // TODO throw a real exception
                    throw new RuntimeException("could not create producer for " + destinationName,
                            e);
                }
            }
        };
    }

    /**
     * Obtains a {@code ClientProducer} appropriate for use by the current thread.
     *
     * @return a {@code ClientProducer}
     */
    public ClientProducer getProducer() {
        return producer.get();
    }

    /**
     * Obtains a {@code ClientSession} appropriate for use by the current thread.
     *
     * @return a {@code ClientSession}
     */
    public ClientSession getSession() {
        return session.get();
    }
}
