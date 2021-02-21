package org.slaq.slaqworx.panoptes.pipeline;

import java.util.Collection;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.multimap.MultiMap;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.event.HeldSecurityEvent;

/**
 * A {@code FunctionEx} which "splits" a {@code Security} into {@code HeldSecurityEvent}s, one for
 * each portoflio known to hold the encountered security.
 *
 * @author jeremy
 */
public class HeldSecuritySplitter implements FunctionEx<Security, Traverser<HeldSecurityEvent>> {
    private static final long serialVersionUID = 1L;

    @Override
    public Traverser<HeldSecurityEvent> applyEx(Security security) {
        AssetCache assetCache = PanoptesApp.getAssetCache();
        MultiMap<SecurityKey, PortfolioKey> heldSecuritiesMap = assetCache.getHeldSecuritiesCache();
        // FIXME probably will want to use async versions here
        assetCache.getSecurityCache().set(security.getKey(), security);
        // FIXME maybe locking is necessary
        Collection<PortfolioKey> holdingPortfolios = heldSecuritiesMap.get(security.getKey());

        return (holdingPortfolios == null ? Traversers.empty()
                : Traversers.traverseStream(holdingPortfolios.stream()
                        .map(k -> new HeldSecurityEvent(k, security.getKey()))));
    }
}
