package org.slaq.slaqworx.panoptes.cache;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;

import org.apache.ignite.spi.IgniteSpiContext;
import org.apache.ignite.spi.IgniteSpiException;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder;

/**
 * {@code DummyDiscoveryIpFinder} is a {@code TcpDiscoveryIpFinder} that does absolutely nothing,
 * suitable for single-node Ignite deployments.
 *
 * @author jeremy
 */
public class DummyDiscoveryIpFinder implements TcpDiscoveryIpFinder {
    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public Collection<InetSocketAddress> getRegisteredAddresses() throws IgniteSpiException {
        return Collections.emptyList();
    }

    @Override
    public void initializeLocalAddresses(Collection<InetSocketAddress> addrs)
            throws IgniteSpiException {
        // nothing to do
    }

    @Override
    public boolean isShared() {
        return true;
    }

    @Override
    public void onSpiContextDestroyed() {
        // nothing to do
    }

    @Override
    public void onSpiContextInitialized(IgniteSpiContext spiCtx) {
        // nothing to do
    }

    @Override
    public void registerAddresses(Collection<InetSocketAddress> addrs) {
        // nothing to do
    }

    @Override
    public void unregisterAddresses(Collection<InetSocketAddress> addrs) {
        // nothing to do
    }
}
