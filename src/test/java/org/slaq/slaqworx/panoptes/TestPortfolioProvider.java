package org.slaq.slaqworx.panoptes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.rule.Rule;

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
     * @param id
     *            the ID of the Portfolio to create, or null to generate an ID
     * @param name
     *            the {@code Portfolio} name/description
     * @param positions
     *            the {@code Positions} comprising the {@code Portfolio}
     * @param benchmark
     *            the (possibly null) {@code Portfoli}o that acts a benchmark for the
     *            {@code Portfolio}
     * @param rules
     *            the (possibly empty) {@code Collection} of {@code Rule}s associated with the
     *            {@code Portfolio}
     */
    public Portfolio newPortfolio(String id, String name, Set<? extends Position> positions,
            Portfolio benchmark, Collection<? extends Rule> rules) {
        Portfolio portfolio =
                new Portfolio(new PortfolioKey(id, 1), name, positions, benchmark, rules);
        portfolioMap.put(portfolio.getKey(), portfolio);

        return portfolio;
    }
}
