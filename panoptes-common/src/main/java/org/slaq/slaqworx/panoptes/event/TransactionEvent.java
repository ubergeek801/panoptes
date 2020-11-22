package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.trade.Transaction;

public class TransactionEvent extends PortfolioCommandEvent {
    private final Transaction transaction;

    public TransactionEvent(long eventId, Transaction transaction) {
        super(eventId, transaction.getPortfolioKey());

        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
