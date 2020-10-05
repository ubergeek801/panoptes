package org.slaq.slaqworx.panoptes.asset;

/**
 * The interface for a service that can determine the market value of some {@code MarketValued}
 * item.
 *
 * @author jeremy
 */
public interface MarketValueProvider {

    /**
     * Obtains the market value of the given holding (generally a {@code Portfolio} or a subset of
     * its {@code Position}s). Once calculated, the value may be cached depending on implementation
     * and context.
     *
     * @param holding
     *            the holding for which to obtain the market value
     * @return the market value of the given holding
     */
    public double getMarketValue(MarketValued holding);
}
