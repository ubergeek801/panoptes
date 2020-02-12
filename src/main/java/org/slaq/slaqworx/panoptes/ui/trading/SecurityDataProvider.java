package org.slaq.slaqworx.panoptes.ui.trading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.util.FakeSet;

/**
 * {@code SecurityDataProvider} is a {@code BackEndDataProvider} that provides {@code Security}
 * data, typically for {@code Grid} consumption, allowing the application of a user-specified
 * filter.
 *
 * @author jeremy
 */
public class SecurityDataProvider extends AbstractBackEndDataProvider<Security, Security> {
    private static final long serialVersionUID = 1L;

    private final AssetCache assetCache;

    private List<SecurityKey> securityKeys;

    /**
     * Creates a new {@code SecurityDataProvider} using the given {@code AssetCache} to obtain data.
     *
     * @param assetCache
     *            the {@code AssetCache} from which to obtain {@code Security} data
     */
    public SecurityDataProvider(AssetCache assetCache) {
        this.assetCache = assetCache;
        setFilter(s -> true);
    }

    /**
     * Specifies a filter to be used when querying {@code Security} data, and signals consumers of a
     * refresh event.
     *
     * @param filter
     *            the filter to be used in the {@code Security} query, or {@code null} to return all
     *            results
     */
    public void setFilter(Predicate<Security> filter) {
        securityKeys = new ArrayList<>(assetCache.getSecurityCache()
                .keySet(e -> filter == null ? true : filter.test(e.getValue())));
        Collections.sort(securityKeys, (s1, s2) -> s1.compareTo(s2));
        refreshAll();
    }

    @Override
    protected Stream<Security> fetchFromBackEnd(Query<Security, Security> query) {
        return assetCache.getSecurityCache()
                .getAll(new FakeSet<>(securityKeys.subList(query.getOffset(),
                        Math.min(query.getOffset() + query.getLimit(), securityKeys.size()))))
                .values().stream();
    }

    @Override
    protected int sizeInBackEnd(Query<Security, Security> query) {
        return securityKeys.size();
    }
}