package org.slaq.slaqworx.panoptes.cache;

import java.util.Map;
import java.util.UUID;

import javax.inject.Singleton;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.PositionProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;

/**
 * {@code PortfolioCache} provides operations for accessing {@code Portfolio} and related data (e.g.
 * {@code Position}s, {@code Securities}) from the distributed cache.
 *
 * @author jeremy
 */
@Singleton
public class PortfolioCache
        implements PortfolioProvider, PositionProvider, RuleProvider, SecurityProvider {
    protected static final String PORTFOLIO_EVALUATION_RESULT_MAP_NAME =
            "portfolioEvaluationResultMap";

    public static final String PORTFOLIO_CACHE_NAME = "portfolio";
    public static final String POSITION_CACHE_NAME = "position";
    public static final String SECURITY_CACHE_NAME = "security";
    public static final String RULE_CACHE_NAME = "rule";

    private final HazelcastInstance hazelcastInstance;

    /**
     * Creates a new {@code PortfolioCache}. Restricted because instances of this class should be
     * obtained through the {@code ApplicationContext}.
     *
     * @param hazelcastInstance
     *            the {@code HazelcastInstance} through which to access cached data
     */
    protected PortfolioCache(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
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
     * Obtains the {@code Portfolio} cache from Hazelcast.
     *
     * @return the Hazelcast {@code Portfolio} cache
     */
    public IMap<PortfolioKey, Portfolio> getPortfolioCache() {
        return hazelcastInstance.getMap(PORTFOLIO_CACHE_NAME);
    }

    /**
     * Obtains the map which provides {@code Portfolio} evaluation results.
     *
     * @return a {@code Map} correlating an evaluation request message ID to its results
     */
    public IMap<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>>
            getPortfolioEvaluationResultMap() {
        return hazelcastInstance.getMap(PORTFOLIO_EVALUATION_RESULT_MAP_NAME);
    }

    @Override
    public MaterializedPosition getPosition(PositionKey key) {
        return getPositionCache().get(key);
    }

    /**
     * Obtains the {@code Position} cache from Hazelcast.
     *
     * @return the Hazelcast {@code Position} cache
     */
    public IMap<PositionKey, MaterializedPosition> getPositionCache() {
        return hazelcastInstance.getMap(POSITION_CACHE_NAME);
    }

    @Override
    public Rule getRule(RuleKey key) {
        return getRuleCache().get(key);
    }

    /**
     * Obtains the {@code Rule} cache from Hazelcast.
     *
     * @return the Hazelcast {@code Rule} cache
     */
    public IMap<RuleKey, Rule> getRuleCache() {
        return hazelcastInstance.getMap(RULE_CACHE_NAME);
    }

    @Override
    public Security getSecurity(SecurityKey key) {
        return getSecurityCache().get(key);
    }

    /**
     * Obtains the {@code Security} cache from Hazelcast.
     *
     * @return the Hazelcast {@code Security} cache
     */
    public IMap<SecurityKey, Security> getSecurityCache() {
        return hazelcastInstance.getMap(SECURITY_CACHE_NAME);
    }
}