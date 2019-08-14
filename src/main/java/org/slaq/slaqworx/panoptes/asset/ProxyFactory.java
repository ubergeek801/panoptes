package org.slaq.slaqworx.panoptes.asset;

import io.micronaut.context.ApplicationContext;

import org.slaq.slaqworx.panoptes.cache.PortfolioCache;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.rule.RuleProxy;

/**
 * {@code ProxyFactory} is a service which provides Proxy types (e.g. {@code PositionProxy},
 * {@code RuleProxy}).
 *
 * @author jeremy
 */
public class ProxyFactory {
    private PositionProvider positionProvider;
    private RuleProvider ruleProvider;
    private final ApplicationContext applicationContext;

    /**
     * Creates a new {@code ProxyFactory} which uses the {@code PortfolioCache} as provided by the
     * specified {@code ApplicationContext} when creating Proxies.
     *
     * @param applicationContext
     *            the {@code ApplicationContext} through which to resolve the {@code PortfolioCache}
     */
    public ProxyFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Creates a new {@code ProxyFactory} which uses the specified Providers when creating Proxies.
     *
     * @param positionProvider
     *            the {@code PositionProvider} to assign to Proxies
     * @param ruleProvider
     *            the {@code RuleProvider} to assign to Proxies
     */
    public ProxyFactory(PositionProvider positionProvider, RuleProvider ruleProvider) {
        this.positionProvider = positionProvider;
        this.ruleProvider = ruleProvider;
        applicationContext = null;
    }

    /**
     * Creates a new {@code PositionProxy} for the given {@code PositionKey}.
     *
     * @param key
     *            the key for which to create a {@code Proxy}
     * @return a new {@code PositionProxy}
     */
    public PositionProxy positionProxy(PositionKey key) {
        return new PositionProxy(key, getPositionProvider());
    }

    /**
     * Creates a new {@code PositionProxy} for the given {@code Position} ID.
     *
     * @param id
     *            the ID for which to create a Proxy
     * @return a new {@code PositionProxy}
     */
    public PositionProxy positionProxy(String id) {
        return positionProxy(new PositionKey(id));
    }

    /**
     * Creates a new {@code RuleProxy} for the given {@code RuleKey}.
     *
     * @param key
     *            the key for which to create a Proxy
     * @return a new {@code RuleProxy}
     */
    public RuleProxy ruleProxy(RuleKey key) {
        return new RuleProxy(key, getRuleProvider());
    }

    /**
     * Creates a new {@code RuleProxy} for the given {@code Rule} ID.
     *
     * @param id
     *            the ID for which to create a Proxy
     * @return a new {@code RuleProxy}
     */
    public RuleProxy ruleProxy(String id) {
        return ruleProxy(new RuleKey(id));
    }

    /**
     * Obtains the {@code PositionProvider}, resolving it from the {@code ApplicationContext} if not
     * available.
     *
     * @return the {@code PositionProvider} to use
     */
    protected PositionProvider getPositionProvider() {
        if (positionProvider == null) {
            positionProvider = applicationContext.getBean(PortfolioCache.class);
        }

        return positionProvider;
    }

    /**
     * Obtains the {@code RuleProvider}, resolving it from the {@code ApplicationContext} if not
     * available.
     *
     * @return the {@code RuleProvider} to use
     */
    protected RuleProvider getRuleProvider() {
        if (ruleProvider == null) {
            ruleProvider = applicationContext.getBean(PortfolioCache.class);
        }

        return ruleProvider;
    }
}
