package org.slaq.slaqworx.panoptes.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.postgresql.util.PSQLException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import com.hazelcast.core.MapStore;

/**
 * HazelcastMapStore is a partial implementation of a MapStore. Interestingly, MapStores are
 * required to implement Serializable although the interface doesn't inherit it, so we do.
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
public abstract class HazelcastMapStore<K, V>
        implements MapStore<K, V>, RowMapper<V>, Serializable {
    protected class IterableKeyExtractor implements Iterable<K>, Iterator<K> {
        private ResultSet rs;
        private int rowNum;

        protected IterableKeyExtractor(ResultSet rs) {
            this.rs = rs;
        }

        @Override
        public boolean hasNext() {
            try {
                if (rs.isClosed()) {
                    return false;
                }
                boolean isLast = rs.isLast();
                if (isLast) {
                    // proactively close the ResultSet
                    rs.close();
                }
                return !isLast;
            } catch (Exception e) {
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                if (rootCause instanceof PSQLException
                        && rootCause.getMessage().contains("closed")) {
                    return false;
                }

                // FIXME handle this better
                throw new RuntimeException("could not determine ResultSet status", e);
            }
        }

        @Override
        public Iterator<K> iterator() {
            return this;
        }

        @Override
        public K next() {
            try {
                rs.next();
                return getKeyMapper().mapRow(rs, ++rowNum);
            } catch (SQLException e) {
                // FIXME handle this better
                throw new RuntimeException("could not get next row from ResultSet", e);
            }
        }
    }

    private static final long serialVersionUID = 1L;

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
    public void deleteAll(Collection<K> keys) {
        keys.forEach(k -> delete(k));
    }

    @Override
    public final V load(K key) {
        return DataAccessUtils.singleResult(
                getJdbcTemplate().query(getLoadQuery(), getLoadParameters(key), this));
    }

    @Override
    public Map<K, V> loadAll(Collection<K> keys) {
        return keys.stream().collect(Collectors.toMap(k -> k, k -> load(k)));
    }

    @Override
    public Iterable<K> loadAllKeys() {
        String keyQuery = "select " + getIdColumnNames() + " from " + getTableName();
        return jdbcTemplate.query(keyQuery,
                (ResultSetExtractor<IterableKeyExtractor>)rs -> new IterableKeyExtractor(rs));
    }

    @Override
    public void storeAll(Map<K, V> map) {
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

    protected abstract RowMapper<K> getKeyMapper();

    protected abstract Object[] getLoadParameters(K key);

    protected abstract String getLoadQuery();

    /**
     * Obtains this MapStore's table name.
     *
     * @return the tableName corresponding to entities serviced by this MapStore
     */
    protected abstract String getTableName();
}
