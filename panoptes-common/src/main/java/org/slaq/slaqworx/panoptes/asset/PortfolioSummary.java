package org.slaq.slaqworx.panoptes.asset;

import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * {@code PortfolioSummary} is a projection of {@code Portfolio} used primarily by the
 * {@code Portfolio} table display.
 *
 * @author jeremy
 */
public class PortfolioSummary implements Keyed<PortfolioKey>, ProtobufSerializable {
    /**
     * Creates a new {@code PortfolioSummary} from the given {@code Portfolio}.
     *
     * @param portfolio
     *            the {@code Portfolio} to summarize
     * @param marketValueProvider
     *            a {@code MarketValueProvider} to use for market value calculations
     * @return a {@code PortfolioSummary} summarizing the given {@code Portfolio}
     */
    public static PortfolioSummary fromPortfolio(Portfolio portfolio,
            MarketValueProvider marketValueProvider) {
        return new PortfolioSummary(portfolio.getKey(), portfolio.getName(),
                portfolio.getBenchmarkKey(), marketValueProvider.getMarketValue(portfolio),
                portfolio.isAbstract());
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PortfolioSummary other = (PortfolioSummary)obj;
        if (benchmarkKey == null) {
            if (other.benchmarkKey != null) {
                return false;
            }
        } else if (!benchmarkKey.equals(other.benchmarkKey)) {
            return false;
        }
        if (isAbstract != other.isAbstract) {
            return false;
        }
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (Double.doubleToLongBits(totalMarketValue)
                != Double.doubleToLongBits(other.totalMarketValue)) {
            return false;
        }
        return true;
    }

    /**
     * Obtains the {@code PortfolioKey} corresponding to the {@code Portfolio}'s benchmark, if it
     * has one.
     *
     * @return the benchmark's {@code PortfolioKey}, or {@code null} if the {@code Portfolio} has no
     *         associated benchmark
     */
    public PortfolioKey getBenchmarkKey() {
        return benchmarkKey;
    }

    @Override
    public PortfolioKey getKey() {
        return key;
    }

    /**
     * Obtains the name/description of the {@code Portfolio}.
     *
     * @return the {@code Portfolio} name
     */
    public String getName() {
        return name;
    }

    /**
     * Obtains the total market value of the {@code Portfolio}.
     *
     * @return the {@code Portfolio}'s total market value
     */
    public double getTotalMarketValue() {
        return totalMarketValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((benchmarkKey == null) ? 0 : benchmarkKey.hashCode());
        result = prime * result + (isAbstract ? 1231 : 1237);
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        long temp;
        temp = Double.doubleToLongBits(totalMarketValue);
        result = prime * result + (int)(temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Indicates whether the {@code Portfolio} is considered abstract, such as a synthetic benchmark
     * or analysis model.
     *
     * @return true if the {@code Portfolio} is considered abstract, false otherwise
     */
    public boolean isAbstract() {
        return isAbstract;
    }
}
