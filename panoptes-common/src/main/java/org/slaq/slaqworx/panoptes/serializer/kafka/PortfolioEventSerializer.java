package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.event.PortfolioEvent;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code PortfolioEvent}.
 *
 * @author jeremy
 */
public class PortfolioEventSerializer extends KafkaSerializer<PortfolioEvent> {
    /**
     * Creates a new {@code PortfolioEventSerializer}. Kafka requires a public default constructor.
     */
    public PortfolioEventSerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.PortfolioEventSerializer());
    }
}
