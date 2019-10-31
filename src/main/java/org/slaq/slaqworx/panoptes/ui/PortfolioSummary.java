package org.slaq.slaqworx.panoptes.ui;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * {@code PortfolioSummary} is a projection of {@code Portfolio} used primarily by the Portfolio
 * table display.
 *
 * @author jeremy
 */
public class PortfolioSummary implements Keyed<PortfolioKey> {
    private final PortfolioKey key;
    private final String name;
    private final PortfolioKey benchmarkKey;
    private final double totalMarketValue;
    private final boolean isAbstract;

    /**
     * Creates a new {@code PortfolioSummary} with the given parameters.
     *
     * @param key
     *            the {@code Portfolio} key
     * @param name
     *            the {@code Portfolio} name
     * @param benchmarkKey
     *            the (possibly {@code null}) key corresponding to the {@code Portfolio}'s benchmark
     * @param totalMarketValue
     *            the total market value of the {@code Portfolio}
     * @param isAbstract
     *            {@code true} if the {@code Portfolio} is considered abstract, {@code false}
     *            otherwise
     */
    public PortfolioSummary(PortfolioKey key, String name, PortfolioKey benchmarkKey,
            double totalMarketValue, boolean isAbstract) {
        this.key = key;
        this.name = name;
        this.benchmarkKey = benchmarkKey;
        this.totalMarketValue = totalMarketValue;
        this.isAbstract = isAbstract;
    }

    public PortfolioKey getBenchmarkKey() {
        return benchmarkKey;
    }

    @Override
    public PortfolioKey getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public double getTotalMarketValue() {
        return totalMarketValue;
    }

    public boolean isAbstract() {
        return isAbstract;
    }
}
