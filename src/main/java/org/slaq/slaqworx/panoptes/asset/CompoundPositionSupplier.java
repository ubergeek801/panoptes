package org.slaq.slaqworx.panoptes.asset;

import java.util.stream.Stream;

/**
 * {@code CompoundPositionSupplier} is a {@code PositionSupplier} that represents the
 * "concatenation" of multiple {@code PositionSupplier}s.
 *
 * @author jeremy
 */
public class CompoundPositionSupplier implements PositionSupplier {
    private final PositionSupplier[] suppliers;

    /**
     * Creates a new {@code CompoundPositionSupplier} concatenating the given suppliers.
     *
     * @param suppliers
     *            the suppliers to be concatenated
     */
    public CompoundPositionSupplier(PositionSupplier... suppliers) {
        this.suppliers = suppliers;
    }

    @Override
    public PortfolioKey getPortfolioKey() {
        // all suppliers are presumed to belong to the same Portfolio, so just return the first
        return suppliers[0].getPortfolioKey();
    }

    @Override
    public Stream<Position> getPositions() {
        Stream<Position> concatStream = suppliers[0].getPositions();
        for (int i = 1; i < suppliers.length; i++) {
            concatStream = Stream.concat(concatStream, suppliers[i].getPositions());
        }

        return concatStream;
    }

    @Override
    public double getTotalMarketValue() {
        double totalMarketValue = 0;
        for (PositionSupplier supplier : suppliers) {
            totalMarketValue += supplier.getTotalMarketValue();
        }

        return totalMarketValue;
    }

    @Override
    public int size() {
        int size = 0;
        for (PositionSupplier supplier : suppliers) {
            size += supplier.size();
        }

        return size;
    }
}