package org.slaq.slaqworx.panoptes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;

/**
 * {@code TestPortfolioProvider} is a {@code PortfolioProvider} suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestPortfolioProvider implements PortfolioProvider {
    private final HashMap<PortfolioKey, Portfolio> portfolioMap = new HashMap<>();

    /**
     * Creates a new {@code TestPortfolioProvider}. Restricted because instances of this class
     * should be obtained through {@code TestUtil}.
     */
    protected TestPortfolioProvider() {
        // nothing to do
    }

    @Override
    public Portfolio getPortfolio(PortfolioKey key) {
        return portfolioMap.get(key);
    }

    /**
     * Creates a new {@code Portfolio} and makes it available through this provider.
     *
     * @param key
     *            the key of the {@code Portfolio} to create, or {@code null} to generate a key
     * @param name
     *            the {@code Portfolio} name/description
     * @param positions
     *            the {@code Position}s comprising the {@code Portfolio}
     * @param benchmark
     *            the (possibly {@code null}) {@code Portfolio} that acts a benchmark for the
     *            {@code Portfolio}
     * @param rules
     *            the (possibly empty) {@code Collection} of {@code Rule}s associated with the
     *            {@code Portfolio}
     * @return a {@code Portfolio} with the specified configuration
     */
    public Portfolio newPortfolio(PortfolioKey key, String name, Set<Position> positions,
            Portfolio benchmark, Collection<? extends ConfigurableRule> rules) {
        Portfolio portfolio = new Portfolio(key, name, positions, benchmark, rules);
        portfolioMap.put(key, portfolio);

        return portfolio;
    }

    /**
     * Creates a new {@code Portfolio} and makes it available through this provider.
     *
     * @param id
     *            the ID of the {@code Portfolio} to create, or {@code null} to generate an ID
     * @param name
     *            the {@code Portfolio} name/description
     * @param positions
     *            the {@code Position}s comprising the {@code Portfolio}
     * @param benchmark
     *            the (possibly {@code null}) {@code Portfolio} that acts a benchmark for the
     *            {@code Portfolio}
     * @param rules
     *            the (possibly empty) {@code Collection} of {@code Rule}s associated with the
     *            {@code Portfolio}
     * @return a {@code Portfolio} with the specified configuration
     */
    public Portfolio newPortfolio(String id, String name, Set<Position> positions,
            Portfolio benchmark, Collection<? extends ConfigurableRule> rules) {
        return newPortfolio(new PortfolioKey(id, 1), name, positions, benchmark, rules);
    }
}
