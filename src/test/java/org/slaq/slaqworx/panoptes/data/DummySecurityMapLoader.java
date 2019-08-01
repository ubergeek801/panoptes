package org.slaq.slaqworx.panoptes.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.hazelcast.core.MapStore;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * DummySecurityMapLoader is a MapStore that initializes the Hazelcast cache with Security data from
 * the PimcoBenchmarkDataSource. (For some reason a MapStore needs to be Serializable.)
 *
 * @author jeremy
 */
public class DummySecurityMapLoader implements MapStore<SecurityKey, Security>, Serializable {
    private static final long serialVersionUID = 1L;

    private transient final PimcoBenchmarkDataSource dataSource;

    /**
     * Creates a new DummySecurityMapLoader.
     *
     * @throws IOException
     *             if Security data could not be loaded
     */
    public DummySecurityMapLoader() throws IOException {
        dataSource = PimcoBenchmarkDataSource.getInstance();
    }

    @Override
    public void delete(SecurityKey key) {
        // nothing to do
    }

    @Override
    public void deleteAll(Collection<SecurityKey> keys) {
        // nothing to do
    }

    @Override
    public Security load(SecurityKey key) {
        return dataSource.getSecurityMap().get(key);
    }

    @Override
    public Map<SecurityKey, Security> loadAll(Collection<SecurityKey> keys) {
        return keys.stream().collect(Collectors.toMap(k -> k, k -> load(k)));
    }

    @Override
    public Iterable<SecurityKey> loadAllKeys() {
        return dataSource.getSecurityMap().keySet();
    }

    @Override
    public void store(SecurityKey key, Security value) {
        // nothing to do
    }

    @Override
    public void storeAll(Map<SecurityKey, Security> map) {
        // nothing to do
    }
}
