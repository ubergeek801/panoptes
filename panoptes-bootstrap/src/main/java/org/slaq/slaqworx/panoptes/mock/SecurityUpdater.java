package org.slaq.slaqworx.panoptes.mock;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Singleton;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.offline.PimcoBenchmarkDataSource;

@Singleton
@Context
@Requires(env = { "security-update" })
public class SecurityUpdater implements ApplicationEventListener<StartupEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityUpdater.class);

    /**
     * Executes the {@code SecurityUpdater} application.
     *
     * @param args
     *            the program arguments (unused)
     */
    public static void main(String[] args) {
        try (ApplicationContext appContext = Micronaut.build(args).mainClass(SecurityUpdater.class)
                .environments("security-update", "offline").args(args).start()) {
            // nothing else to do
        }
    }

    private final KafkaProducer kafkaProducer;

    /**
     * Creates a {@code SecurityUpdater} that publishes using the given {@code KafkaProducer}.
     *
     * @param kafkaProducer
     *            the {@code KafkaProducer} with which to publish events to Kafka
     */
    protected SecurityUpdater(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        SecurityUpdater updater = event.getSource().getBean(SecurityUpdater.class);
        try {
            updater.updateSecurities();
        } catch (Exception e) {
            // FIXME throw a better exception
            throw new RuntimeException("could not perform bootstrap", e);
        }
    }

    /**
     * Publishes random security updates to Kafka.
     *
     * @throws IOException
     *             if the data could not be read
     */
    public void updateSecurities() throws IOException {
        // republish some random securities
        ArrayList<Security> securities =
                new ArrayList<>(PimcoBenchmarkDataSource.getInstance().getSecurityMap().values());
        ArrayList<Security> randomSecurities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            randomSecurities.add(securities.remove((int)(Math.random() * securities.size())));
        }

        LOG.info("publishing {} securities", randomSecurities.size());
        randomSecurities.forEach(s -> kafkaProducer.publishSecurity(s.getKey(), s));
        LOG.info("published securities");
    }
}
