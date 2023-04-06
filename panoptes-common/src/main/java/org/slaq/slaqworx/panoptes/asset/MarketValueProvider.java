package org.slaq.slaqworx.panoptes.asset;

import javax.annotation.Nonnull;

/**
 * The interface for a service that can determine the market value of some {@link MarketValued}
 * item.
 *
 * @author jeremy
 */
public interface MarketValueProvider {
  /**
   * Obtains the market value of the given holding (generally a {@link Portfolio} or a subset of its
   * {@link Position}s). Once calculated, the value may be cached depending on implementation and
   * context.
   *
   * @param holding the holding for which to obtain the market value
   * @return the market value of the given holding
   */
  double getMarketValue(@Nonnull MarketValued holding);
}
