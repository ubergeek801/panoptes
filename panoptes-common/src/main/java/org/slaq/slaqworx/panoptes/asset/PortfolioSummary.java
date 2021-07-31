package org.slaq.slaqworx.panoptes.asset;

import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A projection of {@link Portfolio} used primarily by the {@link Portfolio} table display.
 *
 * @param key
 *     the {@link Portfolio} key
 * @param name
 *     the {@link Portfolio} name
 * @param benchmarkKey
 *     the (possibly {@code null}) key corresponding to the {@link Portfolio}'s benchmark
 * @param totalMarketValue
 *     the total market value of the {@link Portfolio}
 * @param isAbstract
 *     {@code true} if the {@link Portfolio} is considered abstract, {@code false} otherwise
 *
 * @author jeremy
 */
public record PortfolioSummary(@Nonnull PortfolioKey key, @Nonnull String name,
                               PortfolioKey benchmarkKey, double totalMarketValue,
                               boolean isAbstract)
    implements Keyed<PortfolioKey>, ProtobufSerializable {
  /**
   * Creates a new {@link PortfolioSummary} from the given {@link Portfolio}.
   *
   * @param portfolio
   *     the {@link Portfolio} to summarize
   * @param marketValueProvider
   *     a {@link MarketValueProvider} to use for market value calculations
   *
   * @return a {@link PortfolioSummary} summarizing the given {@link Portfolio}
   */
  public static PortfolioSummary fromPortfolio(@Nonnull Portfolio portfolio,
      @Nonnull MarketValueProvider marketValueProvider) {
    return new PortfolioSummary(portfolio.getKey(), portfolio.getName(),
        portfolio.getBenchmarkKey(), marketValueProvider.getMarketValue(portfolio),
        portfolio.isAbstract());
  }

  @Override
  @Nonnull
  public PortfolioKey getKey() {
    return key;
  }

  @Override
  @Nonnull
  public String toString() {
    return "PortfolioSummary[key=" + key + ", name=" + name + ", benchmarkKey=" + benchmarkKey +
        ", totalMarketValue=" + totalMarketValue + ", isAbstract=" + isAbstract + "]";
  }
}
