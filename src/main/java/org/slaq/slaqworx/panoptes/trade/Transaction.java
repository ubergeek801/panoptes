package org.slaq.slaqworx.panoptes.trade;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
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
    private final String id;
    private Trade trade;
    private final Portfolio portfolio;
    private final PositionSet positions;

    /**
     * Creates a new {@code Transaction}, with a generated ID, acting on the given {@code Portfolio}
     * with the given allocations.
     *
     * @param portfolio
     *            the {@code Portfolio} affected by this {@code Transaction}
     * @param allocations
     *            the allocations of the {@code Transaction}
     */
    public Transaction(Portfolio portfolio, Collection<Position> allocations) {
        this(null, portfolio, allocations);
    }

    /**
     * Creates a new {@code Transaction} with the given ID, acting on the given {@code Portfolio}
     * with the given allocations.
     *
     * @param id
     *            the unique ID of the {@code Transaction}
     * @param portfolio
     *            the {@code Portfolio} affected by this {@code Transaction}
     * @param allocations
     *            the allocations of the {@code Transaction}
     */
    public Transaction(String id, Portfolio portfolio, Collection<Position> allocations) {
        this.id = (id == null ? UUID.randomUUID().toString() : id);
        this.portfolio = portfolio;
        positions = new PositionSet(allocations, portfolio);
    }

    /**
     * Obtains this {@code Transaction}'s unique ID.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    @Override
    public Portfolio getPortfolio() {
        return portfolio;
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
    public int size() {
        return positions.size();
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
}
