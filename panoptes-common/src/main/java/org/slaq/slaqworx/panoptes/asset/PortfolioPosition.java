package org.slaq.slaqworx.panoptes.asset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.trade.TaxLot;

/**
 * A {@code Position} that is held by a {@code Portfolio} and, as such, is an aggregate of
 * {@code TaxLot}s.
 *
 * @author jeremy
 */
public class PortfolioPosition extends AbstractPosition {
    private final ArrayList<TaxLot> taxLots;
    private final double amount;

    /**
     * Creates a new {@code PortfolioPosition} aggregating the given {@code TaxLot}s.
     *
     * @param taxLots
     *            the {@code TaxLot}s to be aggregated by this {@code Position}
     */
    public PortfolioPosition(Collection<TaxLot> taxLots) {
        this.taxLots = new ArrayList<>(taxLots);
        amount = this.taxLots.stream().collect(Collectors.summingDouble(TaxLot::getAmount));
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public SecurityKey getSecurityKey() {
        // all TaxLots must be on the same Security, so just take the first
        return taxLots.get(0).getSecurityKey();
    }

    @Override
    public Stream<? extends Position> getTaxLots() {
        return taxLots.stream();
    }
}
