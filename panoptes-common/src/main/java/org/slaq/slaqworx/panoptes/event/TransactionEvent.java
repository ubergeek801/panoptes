package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * A {@link PortfolioCommandEvent} which requests evaluation of a trade {@link Transaction} against
 * the specified portfolio.
 *
 * @author jeremy
 */
public class TransactionEvent extends PortfolioCommandEvent {
  private final Transaction transaction;

  /**
   * Creates a new {@link TransactionEvent}.
   *
   * @param eventId
   *     an ID identifying the event
   * @param transaction
   *     a {@link Transaction} to be evaluated against the specified portfolio
   */
  public TransactionEvent(long eventId, Transaction transaction) {
    super(eventId, transaction.getPortfolioKey());

    this.transaction = transaction;
  }

  /**
   * Obtains the {@link Transaction} to be evaluated.
   *
   * @return a {@link Transaction}
   */
  public Transaction getTransaction() {
    return transaction;
  }
}
