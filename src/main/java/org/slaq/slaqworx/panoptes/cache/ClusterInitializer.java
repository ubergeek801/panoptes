package org.slaq.slaqworx.panoptes.cache;

import io.micronaut.context.event.ApplicationEventPublisher;

import org.apache.ignite.Ignite;
import org.apache.ignite.events.DiscoveryEvent;
import org.apache.ignite.events.EventType;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.ApplicationContextProvider;

public class ClusterInitializer implements Service {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ClusterInitializer.class);

    private transient boolean isActive;

    public ClusterInitializer() {
        // nothing to do
    }

    @Override
    public void cancel(ServiceContext ctx) {
        setActive(false);
    }

    @Override
    public void execute(ServiceContext ctx) throws Exception {
        setActive(true);
    }

    @Override
    public void init(ServiceContext ctx) throws Exception {
        // nothing to do
    }

    protected synchronized boolean isActive() {
        return isActive;
    }

    protected synchronized void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @IgniteInstanceResource
    protected void setIgniteInstance(Ignite igniteInstance) {
        // delay cache loading until there are at least 4 members
        LOG.info("waiting for minimum of 4 cluster members");
        IgnitePredicate<DiscoveryEvent> listener = event -> {
            if (!isActive()) {
                return true;
            }

            int clusterSize = event.topologyNodes().size();
            if (clusterSize < 4) {
                LOG.info("cluster size now {} of 4 expected", clusterSize);
                return true;
            }

            // publishEvent() is synchronous so execute in a separate thread
            new Thread(() -> ApplicationContextProvider.getApplicationContext()
                    .getBean(ApplicationEventPublisher.class).publishEvent(new ClusterReadyEvent()),
                    "cluster-initializer").start();

            // once we've initialized, there's no going back, so no further events needed
            return false;
        };
        igniteInstance.events().localListen(listener, EventType.EVT_NODE_JOINED);
    }
}
