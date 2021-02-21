package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code PortfolioKey}.
 *
 * @author jeremy
 */
public class PortfolioKeySerializer extends KafkaSerializer<PortfolioKey> {
    /**
     * Creates a new {@code PortfolioKeySerializer}. Kafka requires a public default constructor.
     */
    public PortfolioKeySerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.PortfolioKeySerializer());
    }
}
