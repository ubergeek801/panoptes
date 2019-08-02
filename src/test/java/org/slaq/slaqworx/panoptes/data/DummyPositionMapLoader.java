package org.slaq.slaqworx.panoptes.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.hazelcast.core.MapStore;

import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;

/**
 * DummyPositionMapLoader is a MapStore that initializes the Hazelcast cache with random Position
 * data. (For some reason a MapStore needs to be Serializable.) It isn't actually used yet, but
 * eventually Positions within a Portfolio will need to be versioned.
 *
 * @author jeremy
 */
public class DummyPositionMapLoader implements MapStore<String, MaterializedPosition>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public void delete(String key) {
        // nothing to do
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        // nothing to do
    }

    @Override
    public MaterializedPosition load(String key) {
        return null;
    }

    @Override
    public Map<String, MaterializedPosition> loadAll(Collection<String> keys) {
        return null;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        // nothing to do
        return Collections.emptyList();
    }

    @Override
    public void store(String key, MaterializedPosition value) {
        // nothing to do
    }

    @Override
    public void storeAll(Map<String, MaterializedPosition> map) {
        // nothing to do
    }
}
