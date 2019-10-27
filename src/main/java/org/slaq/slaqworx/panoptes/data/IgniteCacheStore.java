package org.slaq.slaqworx.panoptes.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import javax.cache.Cache;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.sql.DataSource;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.cache.affinity.Affinity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * {@code IgniteCacheStore} is a partial implementation of a {@code CacheWriter} which provides
 * efficient implementations for cache loading and {@code storeAll()} (using Ignite data streaming
 * for the former and JDBC batching for the latter).
 *
 * @author jeremy
 * @param <K>
 *            the entity key type
 * @param <V>
 *            the entity value type
 */
public abstract class IgniteCacheStore<K, V extends Keyed<K>>
        implements CacheWriter<K, V>, RowMapper<V> {
    private final Ignite igniteInstance;

    private final IgniteDataStreamer<K, V> dataStreamer;
    private final JdbcTemplate jdbcTemplate;
    private final String cacheName;

    /**
     * Creates a new {@code IgniteCacheStore}.
     *
     * @param igniteInstance
     *            the {@code Ignite} instance for which to stream data
     * @param dataSource
     *            the {@code DataSource} from which to stream data
     * @param cacheName
     *            the name of the cache served by this store
     */
    protected IgniteCacheStore(Ignite igniteInstance, DataSource dataSource, String cacheName) {
        this.igniteInstance = igniteInstance;
        dataStreamer = igniteInstance.dataStreamer(cacheName);
        dataStreamer.allowOverwrite(true);
        jdbcTemplate = new JdbcTemplate(dataSource);
        this.cacheName = cacheName;
    }

    @Override
    public void deleteAll(Collection<?> keys) {
        keys.forEach(k -> delete(k));
    }

    /**
     * Loads/streams all available records from the database into the associated cache.
     *
     * @return the number of records loaded
     */
    public int loadAll() {
        // use affinity to determine which partitions to load
        Affinity<K> cacheAffinity = igniteInstance.affinity(cacheName);
        int[] localPartitions =
                cacheAffinity.primaryPartitions(igniteInstance.cluster().localNode());

        StringBuilder query = new StringBuilder(getLoadSelect());
        // FIXME sometimes the partition list can be empty
        // if (localPartitions.length > 0) {
        // query.append(" where partition_id in (");
        // query.append(IntStream.of(localPartitions).mapToObj(i -> String.valueOf(i))
        // .collect(Collectors.joining(",")));
        // query.append(")");
        // }
        RowCountCallbackHandler rowHandler = new RowCountCallbackHandler() {
            @Override
            protected void processRow(ResultSet rs, int rowNum) throws SQLException {
                V entity = mapRow(rs, rowNum);
                dataStreamer.addData(entity.getKey(), entity);
            }
        };
        try {
            getJdbcTemplate().query(query.toString(), rowHandler);
            return rowHandler.getRowCount();
        } catch (DataAccessException e) {
            throw new CacheLoaderException("could not load cache from " + getTableName(), e);
        } finally {
            dataStreamer.close(false);
        }
    }

    @Override
    public final void write(Cache.Entry<? extends K, ? extends V> entry) {
        write(entry.getKey(), entry.getValue());
    }

    /**
     * Persists the specified entry.
     *
     * @param key
     *            the key identifying the entry
     * @param value
     *            the value of the entry
     */
    public final void write(K key, V value) {
        writeAll(Map.of(key, value));
    }

    @Override
    public void writeAll(Collection<Cache.Entry<? extends K, ? extends V>> entries) {
        writeAll(entries.stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }

    /**
     * Persists the specified entries.
     *
     * @param entries
     *            a {@code Map} of entry key to entry value
     */
    public final void writeAll(Map<? extends K, ? extends V> entries) {
        preWriteAll(entries);
        Iterator<? extends V> entryIter = entries.values().iterator();
        getJdbcTemplate().batchUpdate(getWriteSql(), new BatchPreparedStatementSetter() {
            @Override
            public int getBatchSize() {
                return entries.size();
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                IgniteCacheStore.this.setValues(ps, entryIter.next());
            }
        });
        postWriteAll(entries);
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
     * Obtains the key column(s) for this {@code CacheStore}'s table.
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
     * Obtains the {@code select} portion of the SQL query to be used to load row(s) by ID(s). This
     * portion of the query should not include a {@code where} clause.
     *
     * @return a partial SQL query
     */
    protected abstract String getLoadSelect();

    /**
     * Obtains the cache partition ID for the given key.
     *
     * @param key
     *            the key for which to obtain the partition ID
     * @return the partition ID
     */
    protected short getPartition(K key) {
        return (short)igniteInstance.affinity(cacheName).partition(key);
    }

    /**
     * Obtains this {@code CacheStore}'s table name.
     *
     * @return the table name corresponding to entities serviced by this {@code CacheStore}
     */
    protected abstract String getTableName();

    /**
     * Obtains the insert/update SQL to be used in a write operation. This will be used in
     * conjunction with {@code setValues()} to batch individual operations.
     *
     * @return the SQL to be used for insert/update operations
     */
    protected abstract String getWriteSql();

    /**
     * Invoked prior to {@code writeAll()}.
     *
     * @param entries
     *            a {@code Collection} of entries being written
     */
    protected void postWriteAll(Map<? extends K, ? extends V> entries) {
        // default is to do nothing
    }

    /**
     * Invoked following {@code writeAll()}.
     *
     * @param entries
     *            a {@code Collection} of entries being written
     */
    protected void preWriteAll(Map<? extends K, ? extends V> entries) {
        // default is to do nothing
    }

    /**
     * Sets the values to be inserted/updated for the given value, as part of a batch store
     * operation (with SQL provided by {@code getStoreSql()}.
     *
     * @param ps
     *            the {@code PreparedStatement} on which to set the values
     * @param value
     *            the entity value for which to set the values
     * @throws SQLException
     *             if the values could not be set
     */
    protected abstract void setValues(PreparedStatement ps, V value) throws SQLException;
}
