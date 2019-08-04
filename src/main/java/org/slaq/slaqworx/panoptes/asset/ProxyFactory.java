package org.slaq.slaqworx.panoptes.asset;

import org.springframework.context.ApplicationContext;

import org.slaq.slaqworx.panoptes.data.PortfolioCache;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.rule.RuleProxy;

/**
 * ProxyFactory is a service which provides Proxy types (e.g. PositionProxy, RuleProxy).
 *
 * @author jeremy
 */
public class ProxyFactory {
    private PositionProvider positionProvider;
    private RuleProvider ruleProvider;
    private final ApplicationContext applicationContext;

    /**
     * Creates a new ProxyFactory which uses the PortfolioCache as provided by the specified
     * ApplicationContext when creating Proxies.
     *
     * @param applicationContext
     *            the ApplicationContext through which to resolve the PortfolioCache
     */
    public ProxyFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Creates a new ProxyFactory which uses the specified Providers when creating Proxies.
     *
     * @param positionProvider
     *            the PositionProvider to assign to Proxies
     * @param ruleProvider
     *            the RuleProvider to assign to Proxies
     */
    public ProxyFactory(PositionProvider positionProvider, RuleProvider ruleProvider) {
        this.positionProvider = positionProvider;
        this.ruleProvider = ruleProvider;
        applicationContext = null;
    }

    /**
     * Creates a new PositionProxy for the given PositionKey.
     *
     * @param key
     *            the key for which to create a Proxy
     * @return a new PositionProxy
     */
    public PositionProxy positionProxy(PositionKey key) {
        return new PositionProxy(key, getPositionProvider());
    }

    /**
     * Creates a new PositionProxy for the given Position ID.
     *
     * @param id
     *            the ID for which to create a Proxy
     * @return a new PositionProxy
     */
    public PositionProxy positionProxy(String id) {
        return positionProxy(new PositionKey(id));
    }

    /**
     * Creates a new RuleProxy for the given RuleKey.
     *
     * @param key
     *            the key for which to create a Proxy
     * @return a new RuleProxy
     */
    public RuleProxy ruleProxy(RuleKey key) {
        return new RuleProxy(key, getRuleProvider());
    }

    /**
     * Creates a new RuleProxy for the given Rule ID.
     *
     * @param id
     *            the ID for which to create a Proxy
     * @return a new RuleProxy
     */
    public RuleProxy ruleProxy(String id) {
        return ruleProxy(new RuleKey(id));
    }

    /**
     * Obtains the PositionProvider, resolving it from the ApplicationContext if not available.
     *
     * @return the PositionProvider to use
     */
    protected PositionProvider getPositionProvider() {
        if (positionProvider == null) {
            positionProvider = applicationContext.getBean(PortfolioCache.class);
        }

        return positionProvider;
    }

    /**
     * Obtains the RuleProvider, resolving it from the ApplicationContext if not available.
     *
     * @return the RuleProvider to use
     */
    protected RuleProvider getRuleProvider() {
        if (ruleProvider == null) {
            ruleProvider = applicationContext.getBean(PortfolioCache.class);
        }

        return ruleProvider;
    }
}
