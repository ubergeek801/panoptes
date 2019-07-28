package org.slaq.slaqworx.panoptes.data;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapStore;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;

/**
 * PortfolioCache provides operations for accessing Portfolio and related data (e.g. Positions,
 * Securities) from the distributed cache.
 *
 * @author jeremy
 */
@Service
public class PortfolioCache implements SecurityProvider {
    private static final String PORTFOLIO_CACHE_NAME = "portfolio";
    private static final String POSITION_CACHE_NAME = "position";
    private static final String SECURITY_CACHE_NAME = "security";

    private static final Map<String, Class<? extends MapStore<?, ?>>> MAP_STORES = Map.of(
            PORTFOLIO_CACHE_NAME, DummyPortfolioMapLoader.class, POSITION_CACHE_NAME,
            DummyPositionMapLoader.class, SECURITY_CACHE_NAME, DummySecurityMapLoader.class);

    public static Map<String, Class<? extends MapStore<?, ?>>> getMapStores() {
        return MAP_STORES;
    }

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

    @Override
    public Security getSecurity(SecurityKey key) {
        return getSecurityCache().get(key);
    }

    /**
     * Obtains the Security cache from Hazelcast.
     *
     * @return the Hazelcast Security cache
     */
    protected Map<SecurityKey, Security> getSecurityCache() {
        return hazelcastInstance.getMap(SECURITY_CACHE_NAME);
    }
}
