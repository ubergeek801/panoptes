package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.TransactionEvent;
import org.slaq.slaqworx.panoptes.trade.Trade;

/**
 * A {@code FunctionEx} which splits a {@code Trade} into its constituent {@code Transaction}s,
 * wrapped in {@code TransactionEvent}s for further processing.
 *
 * @author jeremy
 */
public class TradeSplitter implements FunctionEx<Trade, Traverser<PortfolioEvent>> {
  private static final long serialVersionUID = 1L;

  @Override
  public Traverser<PortfolioEvent> applyEx(Trade trade) {
    // FIXME create a proper event ID
    return Traversers.traverseStream(trade.getTransactions().values().stream()
        .map(t -> new TransactionEvent(System.currentTimeMillis(), t)));
  }
}
