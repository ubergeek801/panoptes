package org.slaq.slaqworx.panoptes.pipeline;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.TransactionEvent;
import org.slaq.slaqworx.panoptes.trade.Trade;

/**
 * A {@code FlatMapFunction} which splits a {@code Trade} into its constituent {@code Transaction}s,
 * wrapped in {@code TransactionEvent}s for further processing.
 *
 * @author jeremy
 */
public class TradeSplitter implements FlatMapFunction<Trade, PortfolioEvent> {
  private static final long serialVersionUID = 1L;

  @Override
  public void flatMap(Trade trade, Collector<PortfolioEvent> out) {
    // FIXME create a proper event ID
    trade.getTransactions().values()
        .forEach(t -> out.collect(new TransactionEvent(System.currentTimeMillis(), t)));
  }
}
