package org.slaq.slaqworx.panoptes.pipeline;

import java.io.Serial;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.connector.sink2.WriterInitContext;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Sink} that merely logs the received {@link PortfolioSummary}.
 *
 * @author jeremy
 */
public class PortfolioSummaryPublisher implements Sink<PortfolioSummary> {
  @Serial private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(PanoptesPipeline.class);

  @Override
  public SinkWriter<PortfolioSummary> createWriter(WriterInitContext context) {
    return new SinkWriter<>() {
      @Override
      public void write(PortfolioSummary portfolio, Context context) {
        LOG.info("processed portfolio: {}", portfolio);
      }

      @Override
      public void flush(boolean endOfInput) {
        // nothing to do
      }

      @Override
      public void close() {
        // nothing to do
      }
    };
  }
}
