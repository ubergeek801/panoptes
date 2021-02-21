package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.cache.PortfolioSummarizer;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code PortfolioSummarizer}.
 *
 * @author jeremy
 */
public class PortfolioSummarizerSerializer extends KafkaSerializer<PortfolioSummarizer> {
    /**
     * Creates a new {@code PortfolioSummarizerSerializer}. Kafka requires a public default
     * constructor.
     */
    public PortfolioSummarizerSerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.PortfolioSummarizerSerializer());
    }
}
