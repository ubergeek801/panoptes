package org.slaq.slaqworx.panoptes.data;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.hazelcast.map.MapStore;

import io.micronaut.transaction.SynchronousTransactionManager;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A partial implementation of a {@code MapStore} which provides efficient implementations for
 * {@code loadAll()} and {@code storeAll()} (using SQL {@code IN} clauses for the former and JDBC
 * batching for the latter).
 *
 * @author jeremy
 * @param <K>
 *            the entity key type
 * @param <V>
 *            the entity value type
 */
public abstract class HazelcastMapStore<K, V extends Keyed<K>>
        implements MapStore<K, V>, RowMapper<V> {
    private static final Logger LOG = LoggerFactory.getLogger(HazelcastMapStore.class);

    private final SynchronousTransactionManager<Connection> transactionManager;
    private final Jdbi jdbi;

    /**
     * Creates a new {@code HazelcastMapStore} which uses the given {@code Jdbi} instance.
     *
     * @param transactionManager
     *            the {@code TransactionManager} to use for {@code loadAllKeys()}
     * @param jdbi
     *            the {@code Jdbi} instance to use for database operations
     */
    protected HazelcastMapStore(SynchronousTransactionManager<Connection> transactionManager,
            Jdbi jdbi) {
        this.transactionManager = transactionManager;
        this.jdbi = jdbi;
    }

    @Override
    @Transactional
    public void deleteAll(Collection<K> keys) {
        keys.forEach(this::delete);
    }

    @Override
    @Transactional
    public V load(K key) {
        Map<K, V> values = loadAll(List.of(key));

        return (values.isEmpty() ? null : values.get(0));
    }

    @Override
    @Transactional
    public Map<K, V> loadAll(Collection<K> keys) {
        LOG.info("loading {} {} entities", keys.size(), getTableName());

        String[] keyColumns = getKeyColumnNames();
        StringBuilder queryString = new StringBuilder(getLoadSelect());
        queryString.append(" where (").append(String.join(",", keyColumns) + ") in (values ");

        List<Object> parameters = new ArrayList<>(keys.size() * keyColumns.length);
        int keyIndex = 0;
        for (K key : keys) {
            if (keyIndex > 0) {
                queryString.append(", ");
            }
            queryString.append("(");
            Object[] keyComponents = getKeyComponents(key);
            for (int keyComponentIndex = 0; keyComponentIndex < keyComponents.length;
                    keyComponentIndex++) {
                if (keyComponentIndex > 0) {
                    queryString.append(", ");
                }
                queryString.append("?");
                parameters.add(keyComponents[keyComponentIndex]);
            }
            queryString.append(")");
            keyIndex++;
        }
        queryString.append(")");

        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(queryString.toString());
            for (int i = 0; i < parameters.size(); i++) {
                query.bind(i, parameters.get(i));
            }
            return query.map(this).stream().collect(Collectors.toMap(Keyed::getKey, v -> v));
        });
    }

    @Override
    public Iterable<K> loadAllKeys() {
        // Hazelcast will close the iterator when complete, so we need to leave the transaction and
        // Jdbi open during the meantime (KeyIterable will take care of cleanup)
        return new KeyIterable<>(transactionManager, jdbi,
                "select " + String.join(",", getKeyColumnNames()) + " from " + getTableName(),
                getKeyMapper());
    }

    @Override
    @Transactional
    public void store(K key, V value) {
        storeAll(Map.of(key, value));
    }

    @Override
    @Transactional
    public void storeAll(Map<K, V> map) {
        preStoreAll(map);

        jdbi.withHandle(handle -> {
            PreparedBatch batch = handle.prepareBatch(getStoreSql());
            map.values().forEach(v -> bindValues(batch, v));
            return batch.execute();
        });

        postStoreAll(map);
    }

    /**
     * Binds the values to be inserted/updated for the given value, as part of a batch store
     * operation (with SQL provided by {@code getStoreSql()}.
     *
     * @param batch
     *            the {@code PreparedBatch} on which to bind the values
     * @param value
     *            the entity value for which to bind the values
     */
    protected abstract void bindValues(PreparedBatch batch, V value);

    /**
     * Obtains the {@code Jdbi} to use for database operations.
     *
     * @return a {@code Jdbi}
     */
    protected Jdbi getJdbi() {
        return jdbi;
    }

    /**
     * Obtains the key column(s) for this {@code MapStore}'s table.
     *
     * @return the table's key column names
     */
    protected abstract String[] getKeyColumnNames();

    /**
     * Obtains the component values that comprise the given key.
     *
     * @param key
     *            the key from which to extract component values
     * @return the component values as an {@code Object} array
     */
    protected abstract Object[] getKeyComponents(K key);

    /**
     * Obtains a {@code RowMapper} which can be used to fetch keys for the entity being mapped.
     *
     * @return a {@code RowMapper}
     */
    protected abstract RowMapper<K> getKeyMapper();

    /**
     * Obtains the {@code select} portion of the SQL query to be used to load row(s) by ID(s). This
     * portion of the query should not include a {@code where} clause.
     *
     * @return a partial SQL query
     */
    protected abstract String getLoadSelect();

    /**
     * Obtains the insert/update SQL to be used in a store operation. This will be used in
     * conjunction with {@code setValues()} to batch individual operations.
     *
     * @return the SQL to be used for insert/update operations
     */
    protected abstract String getStoreSql();

    /**
     * Obtains this {@code MapStore}'s table name.
     *
     * @return the table name corresponding to entities serviced by this {@code MapStore}
     */
    protected abstract String getTableName();

    /**
     * Invoked prior to {@code storeAll()}.
     *
     * @param map
     *            a {@code Map} of entity key to entity value, of entities being stored
     */
    protected void postStoreAll(Map<K, V> map) {
        // default is to do nothing
    }

    /**
     * Invoked following {@code storeAll()}.
     *
     * @param map
     *            a {@code Map} of entity key to entity value, of entities being stored
     */
    protected void preStoreAll(Map<K, V> map) {
        // default is to do nothing
    }
}
