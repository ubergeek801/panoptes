package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import java.io.Serial;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.event.SecurityUpdateEvent;

/**
 * A {@link FunctionEx} which broadcasts {@link Security} updates as {@link SecurityUpdateEvent}s,
 * one for each known portfolio (where "known" means "exists in the Portfolio cache").
 *
 * @author jeremy
 */
public class SecurityBroadcaster implements FunctionEx<Security, Traverser<SecurityUpdateEvent>> {
  @Serial
  private static final long serialVersionUID = 1L;

  @Override
  public Traverser<SecurityUpdateEvent> applyEx(Security security) {
    AssetCache assetCache = PanoptesApp.getAssetCache();
    assetCache.getSecurityCache().set(security.getKey(), security);

    return Traversers.traverseStream(assetCache.getPortfolioCache().keySet().stream())
        .map(k -> new SecurityUpdateEvent(k, security.getKey()));
  }
}
