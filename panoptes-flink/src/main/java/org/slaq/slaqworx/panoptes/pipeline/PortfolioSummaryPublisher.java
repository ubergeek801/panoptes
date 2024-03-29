package org.slaq.slaqworx.panoptes.pipeline;

import java.io.Serial;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SinkFunction} that merely logs the received {@link PortfolioSummary}.
 *
 * @author jeremy
 */
public class PortfolioSummaryPublisher implements SinkFunction<PortfolioSummary> {
  @Serial private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(PanoptesPipeline.class);

  @Override
  public void invoke(PortfolioSummary portfolio, Context context) {
    LOG.info("processed portfolio: {}", portfolio);
  }
}
