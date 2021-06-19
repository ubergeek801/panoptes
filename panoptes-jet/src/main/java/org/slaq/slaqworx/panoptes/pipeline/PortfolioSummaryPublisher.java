package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.BiConsumerEx;
import com.hazelcast.jet.pipeline.Sink;
import java.io.Serial;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link BiConsumerEx}, intended for use as a {@link Sink} receive function, which merely logs
 * the received {@link PortfolioSummary}.
 *
 * @author jeremy
 */
public class PortfolioSummaryPublisher implements BiConsumerEx<Void, PortfolioSummary> {
  @Serial
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(PortfolioSummaryPublisher.class);

  @Override
  public void acceptEx(Void context, @Nonnull PortfolioSummary portfolio) {
    LOG.info("processed portfolio: {}", portfolio);
  }
}
