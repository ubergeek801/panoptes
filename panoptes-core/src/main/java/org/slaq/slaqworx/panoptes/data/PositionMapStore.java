package org.slaq.slaqworx.panoptes.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;

/**
 * {@code PositionMapStore} is a Hazelcast {@code MapStore} that provides {@code Position}
 * persistence services.
 *
 * @author jeremy
 */
public class PositionMapStore extends HazelcastMapStore<PositionKey, Position> {
    /**
     * Creates a new {@code PositionMapStore}. Restricted because instances of this class should be
     * created through the {@code HazelcastMapStoreFactory}.
     *
     * @param dataSource
     *            the {@code DataSource} through which to access the database
     */
    protected PositionMapStore(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void delete(PositionKey key) {
        getJdbcTemplate().update("delete from portfolio_position where position_id = ?",
                key.getId());
        getJdbcTemplate().update("delete from " + getTableName() + " where id = ?", key.getId());
    }

    @Override
    public Position mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString(1);
        double amount = rs.getDouble(2);
        String securityId = rs.getString(3);

        return new SimplePosition(new PositionKey(id), amount, new SecurityKey(securityId));
    }

    @Override
    protected String[] getKeyColumnNames() {
        return new String[] { "id" };
    }

    @Override
    protected Object[] getKeyComponents(PositionKey key) {
        return new Object[] { key.getId() };
    }

    @Override
    protected RowMapper<PositionKey> getKeyMapper() {
        return (rs, rowNum) -> new PositionKey(rs.getString(1));
    }

    @Override
    protected String getLoadSelect() {
        return "select id, amount, security_id from " + getTableName();
    }

    @Override
    protected String getStoreSql() {
        return "insert into " + getTableName() + " (id, amount, security_id) values (?, ?, ?)"
                + " on conflict on constraint position_pk do update"
                + " set amount = excluded.amount, security_id = excluded.security_id";
    }

    @Override
    protected String getTableName() {
        return "position";
    }

    @Override
    protected void setValues(PreparedStatement ps, Position position) throws SQLException {
        ps.setString(1, position.getKey().getId());
        ps.setDouble(2, position.getAmount());
        ps.setString(3, position.getSecurityKey().getId());
    }
}
