package org.slaq.slaqworx.panoptes.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.hazelcast.core.MapStore;

import org.slaq.slaqworx.panoptes.asset.Security;

/**
 * DummySecurityMapLoader is a MapStore that initializes the Hazelcast cache with Security data from
 * the PimcoBenchmarkDataSource. (For some reason a MapStore needs to be Serializable.)
 *
 * @author jeremy
 */
public class DummySecurityMapLoader implements MapStore<String, Security>, Serializable {
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
    public void delete(String key) {
        // FIXME implement delete()
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        // FIXME implement deleteAll()
    }

    @Override
    public Security load(String key) {
        return dataSource.getSecurityMap().get(key);
    }

    @Override
    public Map<String, Security> loadAll(Collection<String> keys) {
        return keys.stream().collect(Collectors.toMap(k -> k, k -> load(k)));
    }

    @Override
    public Iterable<String> loadAllKeys() {
        return dataSource.getSecurityMap().keySet();
    }

    @Override
    public void store(String key, Security value) {
        // FIXME implement store()
    }

    @Override
    public void storeAll(Map<String, Security> map) {
        // FIXME implement storeAll()
    }
}
