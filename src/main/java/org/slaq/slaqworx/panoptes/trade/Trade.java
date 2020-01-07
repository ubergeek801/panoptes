package org.slaq.slaqworx.panoptes.trade;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * A {@code Trade} is an aggregate of {@code Transaction}s that modify one or more
 * {@code Portfolio}s by altering (increasing or decreasing) the net position of one or more
 * {@code Securities}.
 *
 * @author jeremy
 */
public class Trade {
    private final TradeKey key;
    private final Map<PortfolioKey, Transaction> transactions;

    /**
     * Creates a new {@code Trade} with a generated ID and consisting of the given
     * {@code Transaction}s.
     *
     * @param transactions
     *            the {@code Transaction}s comprising this {@code Trade}, mapped by
     *            {@code PortfolioKey}
     */
    public Trade(Map<PortfolioKey, Transaction> transactions) {
        this(null, transactions);
    }

    /**
     * Creates a new {@code Trade} with the given ID and consisting of the given
     * {@code Transaction}s.
     *
     * @param key
     *            the unique key to assign to this {@code Trade}, or null to generate one
     * @param transactions
     *            the {@code Transaction}s comprising this {@code Trade}, mapped by
     *            {@code PortfolioKey}
     */
    public Trade(TradeKey key, Map<PortfolioKey, Transaction> transactions) {
        this.key = (key == null ? new TradeKey(null) : key);
        this.transactions = transactions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Trade)) {
            return false;
        }
        Trade other = (Trade)obj;
        return key.equals(other.getKey());
    }

    /**
     * Obtains the total number of allocations over all {@code Transaction}s.
     *
     * @return the total allocation count
     */
    public int getAllocationCount() {
        return transactions.values().stream().collect(Collectors.summingInt(t -> t.size()));
    }

    /**
     * Obtains allocations of this {@code Trade} corresponding to the specified {@code Portfolio}.
     *
     * @return a {@code Stream} of {@code Position}s representing the allocations for the specified
     *         {@code Portfolio}
     */
    public Stream<Position> getAllocations(PortfolioKey portfolioKey) {
        Transaction transaction = transactions.get(portfolioKey);

        if (transaction == null) {
            List<Position> emptyList = Collections.emptyList();
            return emptyList.stream();
        }

        return transaction.getPositions();
    }

    /**
     * Obtains this {@code Trade}'s unique key.
     *
     * @return the key
     */
    public TradeKey getKey() {
        return key;
    }

    /**
     * Obtains the {@code Transaction} corresponding to the specified {@code Portfolio}.
     *
     * @return the {@code Transaction} corresponding to the specified {@code Portfolio}, or
     *         {@code null} if it does not exist
     */
    public Transaction getTransaction(PortfolioKey portfolioKey) {
        return transactions.get(portfolioKey);
    }

    /**
     * Obtains all {@code Transactions} of this {@code Trade} grouped by impacted {@code Portfolio}.
     *
     * @return a {@code Map} of {@code PortfolioKey} to the {@code Transaction} impacting it
     */
    public Map<PortfolioKey, Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
