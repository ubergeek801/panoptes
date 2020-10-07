package org.slaq.slaqworx.panoptes.pipeline;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;

public class PortfolioSummaryPublisher implements SinkFunction<PortfolioSummary> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(PanoptesPipeline.class);

    @Override
    public void invoke(PortfolioSummary portfolio, Context context) {
        LOG.info("processed portfolio: {}", portfolio);
    }
}
