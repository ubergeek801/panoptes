package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.BiConsumerEx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;

/**
 * A {@code BiConsumerEx}, intended for use as a {@code Sink} receive function, which merely logs
 * the received {@code PortfolioSummary}.
 *
 * @author jeremy
 */
public class PortfolioSummaryPublisher implements BiConsumerEx<Void, PortfolioSummary> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(PortfolioSummaryPublisher.class);

    @Override
    public void acceptEx(Void context, PortfolioSummary portfolio) {
        LOG.info("processed portfolio: {}", portfolio);
    }
}
