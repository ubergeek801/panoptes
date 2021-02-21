package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.event.PortfolioEvent;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a {@code PortfolioEvent}.
 *
 * @author jeremy
 */
public class PortfolioEventSerializer extends HazelcastStreamSerializer<PortfolioEvent> {
    /**
     * Creates a new {@code PortfolioEventSerializer}. Hazelcast requires a public default
     * constructor.
     */
    public PortfolioEventSerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.PortfolioEventSerializer());
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.PORTFOLIO_EVENT.ordinal();
    }
}
