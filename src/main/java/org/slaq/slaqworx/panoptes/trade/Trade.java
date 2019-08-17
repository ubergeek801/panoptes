package org.slaq.slaqworx.panoptes.trade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@code Trade} is an aggregate of {@code Transaction}s that modify one or more
 * {@code Portfolio}s by altering (increasing or decreasing) the net position of one or more
 * {@code Securities}.
 *
 * @author jeremy
 */
public class Trade {
    private final String id;
    private final ArrayList<Transaction> transactions = new ArrayList<>();

    /**
     * Creates a new {@code Trade} with a generated ID and consisting of the given
     * {@code Transaction}s.
     *
     * @param transactions
     *            the {@code Transaction}s comprising this {@code Trade}
     */
    public Trade(Collection<Transaction> transactions) {
        this(null, transactions);
    }

    /**
     * Creates a new {@code Trade} with the given ID and consisting of the given
     * {@code Transaction}s.
     *
     * @param id
     *            the unique ID to assign to this {@code Trade}, or null to generate one
     * @param transactions
     *            the {@code Transaction}s comprising this {@code Trade}
     */
    public Trade(String id, Collection<Transaction> transactions) {
        this.id = (id == null ? UUID.randomUUID().toString() : id);
        this.transactions.addAll(transactions);
    }

    /**
     * Obtains the total number of allocations over all {@code Transaction}s.
     *
     * @return the total allocation count
     */
    public int getAllocationCount() {
        return transactions.stream().collect(Collectors.summingInt(t -> t.size()));
    }

    /**
     * Obtains this {@code Trade}'s unique ID.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Obtains the {@code Transaction}s comprising this {@code Trade}.
     *
     * @return a {@code Stream} of {@code Transaction}s comprising this {@code Trade}
     */
    public Stream<Transaction> getTransactions() {
        return transactions.stream();
    }
}
