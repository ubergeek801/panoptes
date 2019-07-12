package org.slaq.slaqworx.panoptes.trade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Trade is an aggregate of Transactions that modify one or more Portfolios by altering
 * (increasing or decreasing) the net position of one or more Securities.
 *
 * @author jeremy
 */
public class Trade {
    private final String id;
    private final ArrayList<Transaction> transactions = new ArrayList<>();

    public Trade(Collection<Transaction> transactions) {
        this(null, transactions);
    }

    public Trade(String id, Collection<Transaction> transactions) {
        this.id = (id == null ? UUID.randomUUID().toString() : id);
        this.transactions.addAll(transactions);
    }

    /**
     * Obtains the total number of allocations over all Transactions.
     *
     * @return the total allocation count
     */
    public int getAllocationCount() {
        return transactions.stream().collect(Collectors.summingInt(t -> t.size()));
    }

    /**
     * Obtains this Trade's unique ID.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    public Stream<Transaction> getTransactions() {
        return transactions.stream();
    }
}
