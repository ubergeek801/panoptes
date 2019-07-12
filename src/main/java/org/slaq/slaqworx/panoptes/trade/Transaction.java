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
 * or decreasing) the net position of one or more of its Securities.
 *
 * @author jeremy
 */
public class Transaction implements PositionSupplier {
    private final String id;
    private Trade trade;
    private final Portfolio portfolio;
    private final PositionSet positions;

    public Transaction(Portfolio portfolio, Collection<Position> positions) {
        this(null, portfolio, positions);
    }

    public Transaction(String id, Portfolio portfolio, Collection<Position> positions) {
        this.id = (id == null ? UUID.randomUUID().toString() : id);
        this.portfolio = portfolio;
        this.positions = new PositionSet(positions, portfolio);
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
