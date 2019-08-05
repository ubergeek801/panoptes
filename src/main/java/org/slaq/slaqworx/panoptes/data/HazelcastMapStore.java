package org.slaq.slaqworx.panoptes.data;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.hazelcast.core.MapStore;

/**
 * HazelcastMapStore is a partial implementation of a MapStore.
 * <p>
 * HazelcastMapStore provides default implementations for the <code>*All</code> methods (e.g.
 * <code>loadAll()</code>) which delegate to the single-key operations, but subclasses may override
 * these if they are able to implement these operations more efficiently.
 *
 * @author jeremy
 * @param <K>
 *            the entity key type
 * @param <V>
 *            the entity value type
 */
public abstract class HazelcastMapStore<K, V> implements MapStore<K, V>, RowMapper<V> {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new HazelcastMapStore which uses the given DataSource.
     *
     * @param dataSource
     *            the DataSource to use for database operations
     */
    protected HazelcastMapStore(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public synchronized void deleteAll(Collection<K> keys) {
        keys.forEach(k -> delete(k));
    }

    @Override
    public synchronized final V load(K key) {
        return DataAccessUtils.singleResult(
                getJdbcTemplate().query(getLoadQuery(), getLoadParameters(key), this));
    }

    @Override
    public synchronized Map<K, V> loadAll(Collection<K> keys) {
        return keys.stream().collect(Collectors.toMap(k -> k, k -> load(k)));
    }

    @Override
    public Iterable<K> loadAllKeys() {
        try {
            return new KeyIterator<>(
                    jdbcTemplate.getDataSource().getConnection().prepareStatement(
                            "select " + getIdColumnNames() + " from " + getTableName()),
                    getKeyMapper());
        } catch (SQLException e) {
            // TODO throw a better exception
            throw new RuntimeException("could not get keys for " + getTableName(), e);
        }
    }

    @Override
    public synchronized void storeAll(Map<K, V> map) {
        map.forEach((k, v) -> store(k, v));
    }

    /**
     * Obtains the ID column(s) for this MapStore's table.
     *
     * @return a SQL-friendly representation of the table's ID column names
     */
    protected abstract String getIdColumnNames();

    /**
     * Obtains the JdbcTemplate to use for database operations.
     *
     * @return a JdbcTemplate
     */
    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * Obtains a RowMapper which can be used to fetch keys for the entity being mapped.
     *
     * @return a RowMapper
     */
    protected abstract RowMapper<K> getKeyMapper();

    /**
     * Obtains the parameters that should be used in the SQL query returned by
     * <code>getLoadQuery()</code>.
     *
     * @param key
     *            the key being loaded
     * @return the parameters to be used in the load query
     */
    protected abstract Object[] getLoadParameters(K key);

    /**
     * Obtains the SQL query to be used to load a single row.
     *
     * @return a SQL query
     */
    protected abstract String getLoadQuery();

    /**
     * Obtains this MapStore's table name.
     *
     * @return the tableName corresponding to entities serviced by this MapStore
     */
    protected abstract String getTableName();
}
