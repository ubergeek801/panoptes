package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.event.SecurityUpdateEvent;

/**
 * A {@code FunctionEx} which broadcasts {@code Security} updates as {@code HeldSecurityEvent}s, one
 * for each known portfolio.
 *
 * @author jeremy
 */
public class SecurityBroadcaster implements FunctionEx<Security, Traverser<SecurityUpdateEvent>> {
    private static final long serialVersionUID = 1L;

    @Override
    public Traverser<SecurityUpdateEvent> applyEx(Security security) {
        AssetCache assetCache = PanoptesApp.getAssetCache();
        // FIXME probably will want to use async versions here
        assetCache.getSecurityCache().set(security.getKey(), security);

        return Traversers.traverseStream(assetCache.getPortfolioCache().keySet().stream())
                .map(k -> new SecurityUpdateEvent(k, security.getKey()));
    }
}
