package org.slaq.slaqworx.panoptes.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * A {@link PortfolioProvider} suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestPortfolioProvider implements PortfolioProvider {
  private final HashMap<PortfolioKey, Portfolio> portfolioMap = new HashMap<>();

  /**
   * Creates a new {@link TestPortfolioProvider}. Restricted because instances of this class should
   * be obtained through {@link TestUtil}.
   */
  protected TestPortfolioProvider() {
    // nothing to do
  }

  @Override
  public Portfolio getPortfolio(@Nonnull PortfolioKey key) {
    return portfolioMap.get(key);
  }

  /**
   * Creates a new {@link Portfolio} and makes it available through this provider.
   *
   * @param key
   *     the key of the {@link Portfolio} to create, or {@code null} to generate a key
   * @param name
   *     the {@link Portfolio} name/description
   * @param positions
   *     the {@link Position}s comprising the {@link Portfolio}
   * @param benchmark
   *     the (possibly {@code null}) {@link Portfolio} that acts a benchmark for the {@link
   *     Portfolio}
   * @param rules
   *     the (possibly empty) {@link Collection} of {@link Rule}s associated with the {@link
   *     Portfolio}
   *
   * @return a {@link Portfolio} with the specified configuration
   */
  public Portfolio newPortfolio(PortfolioKey key, String name, Set<Position> positions,
      Portfolio benchmark, Collection<? extends ConfigurableRule> rules) {
    Portfolio portfolio = new Portfolio(key, name, positions, benchmark, rules);
    portfolioMap.put(key, portfolio);

    return portfolio;
  }

  /**
   * Creates a new {@link Portfolio} and makes it available through this provider.
   *
   * @param id
   *     the ID of the {@link Portfolio} to create, or {@code null} to generate an ID
   * @param name
   *     the {@link Portfolio} name/description
   * @param positions
   *     the {@link Position}s comprising the {@link Portfolio}
   * @param benchmark
   *     the (possibly {@code null}) {@link Portfolio} that acts a benchmark for the {@link
   *     Portfolio}
   * @param rules
   *     the (possibly empty) {@link Collection} of {@link Rule}s associated with the {@link
   *     Portfolio}
   *
   * @return a {@link Portfolio} with the specified configuration
   */
  public Portfolio newPortfolio(String id, String name, Set<Position> positions,
      Portfolio benchmark, Collection<? extends ConfigurableRule> rules) {
    return newPortfolio(new PortfolioKey(id, 1), name, positions, benchmark, rules);
  }
}
