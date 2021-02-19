package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * A {@code PortfolioCommandEvent} which requests evaluation of a trade {@code Transaction} against
 * the specified portfolio.
 *
 * @author jeremy
 */
public class TransactionEvent extends PortfolioCommandEvent {
    private final Transaction transaction;

    /**
     * Creates a new {@code TransactionEvent}.
     *
     * @param eventId
     *            an ID identifying the event
     * @param transaction
     *            a {@code Transaction} to be evaluated against the specified portfolio
     */
    public TransactionEvent(long eventId, Transaction transaction) {
        super(eventId, transaction.getPortfolioKey());

        this.transaction = transaction;
    }

    /**
     * Obtains the {@code Transaction} to be evaluated.
     *
     * @return a {@code Transaction}
     */
    public Transaction getTransaction() {
        return transaction;
    }
}
