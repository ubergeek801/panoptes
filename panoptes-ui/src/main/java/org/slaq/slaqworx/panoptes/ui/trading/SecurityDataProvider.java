package org.slaq.slaqworx.panoptes.ui.trading;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.cache.SecurityFilter;
import org.slaq.slaqworx.panoptes.util.FakeSet;

/**
 * A {@link BackEndDataProvider} that provides {@link Security} data, typically for {@link Grid}
 * consumption, allowing the application of a user-specified filter.
 *
 * @author jeremy
 */
public class SecurityDataProvider extends AbstractBackEndDataProvider<Security, Void> {
  @Serial private static final long serialVersionUID = 1L;

  private final AssetCache assetCache;

  private List<SecurityKey> securityKeys;

  /**
   * Creates a new {@link SecurityDataProvider} using the given {@link AssetCache} to obtain data.
   *
   * @param assetCache the {@link AssetCache} from which to obtain {@link Security} data
   */
  public SecurityDataProvider(AssetCache assetCache) {
    this.assetCache = assetCache;
    setFilter(null);
  }

  /**
   * Specifies a filter to be used when querying {@link Security} data, and signals consumers of a
   * refresh event.
   *
   * @param filter the filter to be used in the {@link Security} query, or {@code null} to return
   *     all results
   */
  public void setFilter(SecurityFilter filter) {
    Set<SecurityKey> matchingKeys;
    if (filter == null) {
      // no filtering
      matchingKeys = assetCache.getSecurityCache().keySet();
    } else {
      matchingKeys = assetCache.getSecurityCache().keySet(filter);
    }
    securityKeys = new ArrayList<>(matchingKeys);
    securityKeys.sort(SecurityKey::compareTo);
    refreshAll();
  }

  @Override
  protected Stream<Security> fetchFromBackEnd(Query<Security, Void> query) {
    return assetCache
        .getSecurityCache()
        .getAll(
            new FakeSet<>(
                securityKeys.subList(
                    query.getOffset(),
                    Math.min(query.getOffset() + query.getLimit(), securityKeys.size()))))
        .values()
        .stream();
  }

  @Override
  protected int sizeInBackEnd(Query<Security, Void> query) {
    return securityKeys.size();
  }
}
