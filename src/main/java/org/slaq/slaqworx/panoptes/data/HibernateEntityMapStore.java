package org.slaq.slaqworx.panoptes.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.MapStore;

import org.slaq.slaqworx.panoptes.util.Keyed;

public class HibernateEntityMapStore<ID, T extends Keyed<ID>>
        implements MapStore<ID, T>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(HibernateEntityMapStore.class);

    private final String mapName;
    private final CrudRepositoryWithAllIdsQuery<T, ID> repository;

    /**
     * Creates a new HibernateEntityMapLoader using the given PortfolioRepository. Restricted
     * because only classes in the containing package need to create instances.
     *
     * @param mapName
     *            the name of the map to be loaded (used for logging purposes only)
     * @param repository
     *            the PortfolioRepository to use for CRUD
     */
    protected HibernateEntityMapStore(String mapName,
            CrudRepositoryWithAllIdsQuery<T, ID> repository) {
        this.mapName = mapName;
        this.repository = repository;
    }

    @Override
    public void delete(ID key) {
        repository.deleteById(key);
    }

    @Override
    public void deleteAll(Collection<ID> keys) {
        keys.forEach(k -> delete(k));
    }

    @Override
    public T load(ID key) {
        Optional<T> portfolio = repository.findById(key);
        return (portfolio.isPresent() ? portfolio.get() : null);
    }

    @Override
    public Map<ID, T> loadAll(Collection<ID> keys) {
        return StreamSupport.stream(repository.findAllById(keys).spliterator(), false)
                .collect(Collectors.toMap(p -> p.getId(), p -> p));
    }

    @Override
    public Iterable<ID> loadAllKeys() {
        LOG.info("loading {} keys for cache \"{}\"", repository.count(), mapName);
        return repository.getAllIds();
    }

    @Override
    public void store(ID key, T portfolio) {
        repository.save(portfolio);
    }

    @Override
    public void storeAll(Map<ID, T> map) {
        repository.saveAll(map.values());
    }
}
