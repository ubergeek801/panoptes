package org.slaq.slaqworx.panoptes.trade;

import java.util.Collection;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;

/**
 * A {@code Transaction} is a component of a {@code Trade} which modifies a single {@code Portfolio}
 * by altering (increasing or decreasing) the net position of one or more of its {@code Securities}.
 * The {@code Positions} of a {@code Trade} are also known as allocations.
 *
 * @author jeremy
 */
public class Transaction implements PositionSupplier {
    private final TransactionKey key;
    private Trade trade;
    private final PortfolioKey portfolioKey;
    private final PositionSet positions;

    /**
     * Creates a new {@code Transaction}, with a generated ID, acting on the given {@code Portfolio}
     * with the given allocations.
     *
     * @param portfolioKey
     *            the {@code PortfolioKey} identifying the {@code Portfolio} affected by this
     *            {@code Transaction}
     * @param allocations
     *            the allocations of the {@code Transaction}
     */
    public Transaction(PortfolioKey portfolioKey, Collection<Position> allocations) {
        this(null, portfolioKey, allocations);
    }

    /**
     * Creates a new {@code Transaction} with the given ID, acting on the given {@code Portfolio}
     * with the given allocations.
     *
     * @param key
     *            the unique key of the {@code Transaction}
     * @param portfolioKey
     *            the {@code PortfolioKey} identifying the {@code Portfolio} affected by this
     *            {@code Transaction}
     * @param allocations
     *            the allocations of the {@code Transaction}
     */
    public Transaction(TransactionKey key, PortfolioKey portfolioKey,
            Collection<Position> allocations) {
        this.key = (key == null ? new TransactionKey(null) : key);
        this.portfolioKey = portfolioKey;
        positions = new PositionSet(allocations, portfolioKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Transaction)) {
            return false;
        }
        Transaction other = (Transaction)obj;
        return key.equals(other.getKey());
    }

    /**
     * Obtains this {@code Transaction}'s unique key.
     *
     * @return the ID
     */
    public TransactionKey getKey() {
        return key;
    }

    @Override
    public PortfolioKey getPortfolioKey() {
        return portfolioKey;
    }

    @Override
    public Stream<Position> getPositions() {
        return positions.getPositions();
    }

    @Override
    public double getTotalMarketValue() {
        return positions.getTotalMarketValue();
    }

    public Trade getTrade() {
        return trade;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * Specifies the {@code Trade} associated with this {@code Transaction}.
     *
     * @param trade
     *            the {@code Trade} containing this {@code Transaction}
     */
    protected void setTrade(Trade trade) {
        this.trade = trade;
    }

    @Override
    public int size() {
        return positions.size();
    }
}
