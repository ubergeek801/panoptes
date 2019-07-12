package org.slaq.slaqworx.panoptes.trade;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;

/**
 * A Transaction is a component of a Trade which modifies a single Portfolio by altering (increasing
 * or decreasing) the net position of one or more of its Securities. The Positions of a Trade are
 * also known as allocations.
 *
 * @author jeremy
 */
public class Transaction implements PositionSupplier {
    private final String id;
    private Trade trade;
    private final Portfolio portfolio;
    private final PositionSet positions;

    /**
     * Creates a new Transaction, with a generated ID, acting on the given Portfolio with the given
     * allocations.
     *
     * @param portfolio
     *            the Portfolio impacted by this Transaction
     * @param allocations
     *            the allocations of the Transaction
     */
    public Transaction(Portfolio portfolio, Collection<Position> allocations) {
        this(null, portfolio, allocations);
    }

    /**
     * Creates a new Transaction with the given ID, acting on the given Portfolio with the given
     * allocations.
     *
     * @param id
     *            the unique ID of the Transaction
     * @param portfolio
     *            the Portfolio impacted by this Transaction
     * @param allocations
     *            the allocations of the Transaction
     */
    public Transaction(String id, Portfolio portfolio, Collection<Position> allocations) {
        this.id = (id == null ? UUID.randomUUID().toString() : id);
        this.portfolio = portfolio;
        positions = new PositionSet(allocations, portfolio);
    }

    /**
     * Obtains this Transaction unique ID.
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
    public double getTotalAmount() {
        return positions.getTotalAmount();
    }

    public Trade getTrade() {
        return trade;
    }

    protected void setTrade(Trade trade) {
        this.trade = trade;
    }

    @Override
    public int size() {
        return positions.size();
    }
}
