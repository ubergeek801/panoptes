package org.slaq.slaqworx.panoptes.cache;

import javax.inject.Singleton;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.PositionProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.trade.TradeProvider;

/**
 * {@code AssetCache} provides operations for accessing {@code Portfolio} and related data (e.g.
 * {@code Position}s, {@code Securities}) from the distributed cache.
 *
 * @author jeremy
 */
@Singleton
public class AssetCache implements PortfolioProvider, PositionProvider, RuleProvider,
        SecurityProvider, TradeProvider {
    public static final String PORTFOLIO_CACHE_NAME = "portfolio";
    public static final String POSITION_CACHE_NAME = "position";
    public static final String SECURITY_CACHE_NAME = "security";
    public static final String RULE_CACHE_NAME = "rule";
    public static final String TRADE_CACHE_NAME = "trade";

    private final Ignite ignite;

    /**
     * Creates a new {@code AssetCache}. Restricted because instances of this class should be
     * obtained through the {@code ApplicationContext}.
     *
     * @param igniteInstance
     *            the {@code Ignite} through which to access cached data
     */
    protected AssetCache(Ignite igniteInstance) {
        ignite = igniteInstance;
    }

    /**
     * Obtains the {@code Portfolio} with the given key.
     *
     * @param key
     *            the key for which to obtain the {@code Portfolio}
     * @return the {@code Portfolio} corresponding to the given key, or null if it does not exist
     */
    @Override
    public Portfolio getPortfolio(PortfolioKey key) {
        return getPortfolioCache().get(key);
    }

    /**
     * Obtains the {@code Portfolio} cache from Ignite.
     *
     * @return the Ignite {@code Portfolio} cache
     */
    public IgniteCache<PortfolioKey, Portfolio> getPortfolioCache() {
        return ignite.cache(PORTFOLIO_CACHE_NAME);
    }

    @Override
    public Position getPosition(PositionKey key) {
        return getPositionCache().get(key);
    }

    /**
     * Obtains the {@code Position} cache from Ignite.
     *
     * @return the Ignite {@code Position} cache
     */
    public IgniteCache<PositionKey, Position> getPositionCache() {
        return ignite.cache(POSITION_CACHE_NAME);
    }

    @Override
    public ConfigurableRule getRule(RuleKey key) {
        return getRuleCache().get(key);
    }

    /**
     * Obtains the {@code Rule} cache from Ignite.
     *
     * @return the Ignite {@code Rule} cache
     */
    public IgniteCache<RuleKey, ConfigurableRule> getRuleCache() {
        return ignite.cache(RULE_CACHE_NAME);
    }

    @Override
    public Security getSecurity(SecurityKey key) {
        return getSecurityCache().get(key);
    }

    /**
     * Obtains the {@code Security} cache from Ignite.
     *
     * @return the Ignite {@code Security} cache
     */
    public IgniteCache<SecurityKey, Security> getSecurityCache() {
        return ignite.cache(SECURITY_CACHE_NAME);
    }

    @Override
    public Trade getTrade(TradeKey key) {
        return getTradeCache().get(key);
    }

    /**
     * Obtains the {@code Trade} cache from Ignite.
     *
     * @return the Ignite {@code Trade} cache
     */
    public IgniteCache<TradeKey, Trade> getTradeCache() {
        return ignite.cache(TRADE_CACHE_NAME);
    }
}
