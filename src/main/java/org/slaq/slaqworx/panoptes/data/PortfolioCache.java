package org.slaq.slaqworx.panoptes.data;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;

/**
 * PortfolioCache provides operations for accessing Portfolio and related data (e.g. Positions,
 * Securities) from the distributed cache.
 *
 * @author jeremy
 */
@Service
public class PortfolioCache implements PortfolioProvider, RuleProvider, SecurityProvider {
    protected static final String PORTFOLIO_CACHE_NAME = "portfolio";
    protected static final String POSITION_CACHE_NAME = "position";
    protected static final String SECURITY_CACHE_NAME = "security";
    protected static final String RULE_CACHE_NAME = "rule";

    private final HazelcastInstance hazelcastInstance;

    /**
     * Creates a new PortfolioCache. Restricted because instances of this class should be obtained
     * through Spring.
     *
     * @param hazelcastInstance
     *            the HazelcastInstance through which to access cached data
     */
    protected PortfolioCache(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    /**
     * Obtains the Portfolio with the given key.
     *
     * @param key
     *            the key for which to obtain the Portfolio
     * @return the Portfolio corresponding to the given key, or null if it does not exist
     */
    @Override
    public Portfolio getPortfolio(PortfolioKey key) {
        return getPortfolioCache().get(key);
    }

    /**
     * Obtains the Portfolio cache from Hazelcast.
     *
     * @return the Hazelcast Portfolio cache
     */
    public Map<PortfolioKey, Portfolio> getPortfolioCache() {
        return hazelcastInstance.getMap(PORTFOLIO_CACHE_NAME);
    }

    @Override
    public Rule getRule(RuleKey key) {
        return getRuleCache().get(key);
    }

    /**
     * Obtains the Rule cache from Hazelcast.
     *
     * @return the Hazelcast Rule cache
     */
    public Map<RuleKey, Rule> getRuleCache() {
        return hazelcastInstance.getMap(PORTFOLIO_CACHE_NAME);
    }

    @Override
    public Security getSecurity(SecurityKey key) {
        return getSecurityCache().get(key);
    }

    /**
     * Obtains the Security cache from Hazelcast.
     *
     * @return the Hazelcast Security cache
     */
    public Map<SecurityKey, Security> getSecurityCache() {
        return hazelcastInstance.getMap(SECURITY_CACHE_NAME);
    }
}