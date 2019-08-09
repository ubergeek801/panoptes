package org.slaq.slaqworx.panoptes.data;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.springframework.jdbc.core.RowMapper;

/**
 * KeyIterator is an {@code Iterator} (which is itself {@code Iterable}) which facilitates
 * loading of keys iteratively (rather than loading the complete set as a {@code List}). A
 * {@code HazelcastMapStore} could use it in the following way:
 *
 * <pre>
 * &#64;Override
 * public Iterable<K> loadAllKeys() {
 *     try {
 *         return new KeyIterator<K>(
 *                 jdbcTemplate.getDataSource().getConnection().prepareStatement(
 *                         "select " + getIdColumnNames() + " from " + getTableName()),
 *                 getKeyMapper());
 *     } catch (SQLException e) {
 *         ...
 *     }
 * }
 * </pre>
 *
 * @author jeremy
 * @param <K>
 *            the class implemented by the entity key
 */
public class KeyIterator<K> implements Iterator<K>, Iterable<K>, Closeable {
    private final RowMapper<K> keyMapper;
    private final Connection connection;
    private final ResultSet resultSet;
    private int rowNum;

    public KeyIterator(PreparedStatement statement, RowMapper<K> keyMapper) throws SQLException {
        this.keyMapper = keyMapper;

        connection = statement.getConnection();
        statement.execute();
        resultSet = statement.getResultSet();
        rowNum = 0;
    }

    @Override
    public void close() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            // ignore
        }
        try {
            connection.close();
        } catch (SQLException e) {
            // ignore
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return !resultSet.isLast();
        } catch (SQLException e) {
            // FIXME throw a better exception
            throw new RuntimeException("could not determine hasNext() status", e);
        }
    }

    @Override
    public Iterator<K> iterator() {
        return this;
    }

    @Override
    public K next() {
        try {
            resultSet.next();

            return keyMapper.mapRow(resultSet, ++rowNum);
        } catch (SQLException e) {
            // FIXME throw a better exception
            throw new RuntimeException("could not get next key", e);
        }
    }
}
