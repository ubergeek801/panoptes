package org.slaq.slaqworx.panoptes.data;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.hazelcast.core.MapStore;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * {@code HazelcastMapStore} is a partial implementation of a {@code MapStore}.
 * <p>
 * {@code HazelcastMapStore} provides default implementations for the {@code *All} methods (e.g.
 * {@code loadAll()}) which delegate to the single-key operations, but subclasses may override these
 * if they are able to implement these operations more efficiently.
 *
 * @author jeremy
 * @param <K>
 *            the entity key type
 * @param <V>
 *            the entity value type
 */
public abstract class HazelcastMapStore<K, V extends Keyed<K>>
        implements MapStore<K, V>, RowMapper<V> {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new {@code HazelcastMapStore} which uses the given {@code DataSource}.
     *
     * @param dataSource
     *            the {@code DataSource} to use for database operations
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
        Map<K, V> values = loadAll(List.of(key));

        return (values.isEmpty() ? null : values.get(0));
    }

    @Override
    public Map<K, V> loadAll(Collection<K> keys) {
        String[] keyColumns = getKeyColumnNames();
        StringBuilder query = new StringBuilder(getLoadSelect());
        query.append(" where (").append(String.join(",", keyColumns) + ") in (values ");

        Object[] parameters = new Object[(keys.size() * keyColumns.length)];
        int keyIndex = 0;
        int parameterIndex = 0;
        for (K key : keys) {
            if (keyIndex > 0) {
                query.append(", ");
            }
            query.append("(");
            Object[] keyComponents = getKeyComponents(key);
            for (int keyComponentIndex =
                    0; keyComponentIndex < keyComponents.length; keyComponentIndex++) {
                if (keyComponentIndex > 0) {
                    query.append(", ");
                }
                query.append("?");
                parameters[parameterIndex++] = keyComponents[keyComponentIndex];
            }
            query.append(")");
            keyIndex++;
        }
        query.append(")");

        return getJdbcTemplate().query(query.toString(), parameters, this).stream()
                .collect(Collectors.toMap(v -> v.getKey(), v -> v));
    }

    @Override
    public Iterable<K> loadAllKeys() {
        try {
            return new KeyIterator<>(
                    jdbcTemplate.getDataSource().getConnection().prepareStatement("select "
                            + String.join(",", getKeyColumnNames()) + " from " + getTableName()),
                    getKeyMapper());
        } catch (SQLException e) {
            // TODO throw a better exception
            throw new RuntimeException("could not get keys for " + getTableName(), e);
        }
    }

    @Override
    public void storeAll(Map<K, V> map) {
        map.forEach((k, v) -> store(k, v));
    }

    /**
     * Obtains the {@code JdbcTemplate} to use for database operations.
     *
     * @return a {@code JdbcTemplate}
     */
    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
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
     * Obtains this {@code MapStore}'s table name.
     *
     * @return the table name corresponding to entities serviced by this {@code MapStore}
     */
    protected abstract String getTableName();
}
