package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code PortfolioSummary}.
 *
 * @author jeremy
 */
public class PortfolioSummarySerializer extends KafkaSerializer<PortfolioSummary> {
    /**
     * Creates a new {@code PortfolioSummarySerializer}. Kafka requires a public default
     * constructor.
     */
    public PortfolioSummarySerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.PortfolioSummarySerializer());
    }
}
