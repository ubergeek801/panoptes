package org.slaq.slaqworx.panoptes.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * PositionMapStore is a Hazelcast MapStore that provides Position persistence services.
 *
 * @author jeremy
 */
@Service
public class PositionMapStore extends HazelcastMapStore<PositionKey, Position> {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new PositionMapStore. Restricted because instances of this class should be created
     * through Spring.
     *
     * @param dataSource
     *            the DataSource through which to access the database
     */
    protected PositionMapStore(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void delete(PositionKey key) {
        // FIXME implement delete()
    }

    @Override
    public Position mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString(1);
        double amount = rs.getDouble(2);
        String securityId = rs.getString(3);

        return new Position(new PositionKey(id), amount, new SecurityKey(securityId));
    }

    @Override
    public void store(PositionKey key, Position position) {
        getJdbcTemplate().update(
                "insert into " + getTableName() + " (id, amount, security_id) values (?, ?, ?)"
                        + " on conflict on constraint position_pk do update"
                        + " set amount = excluded.amount, security_id = excluded.security_id",
                key.getId(), position.getAmount(), position.getSecurityKey().getId());
    }

    @Override
    protected String getIdColumnNames() {
        return "id";
    }

    @Override
    protected RowMapper<PositionKey> getKeyMapper() {
        return (rs, rowNum) -> new PositionKey(rs.getString(1));
    }

    @Override
    protected Object[] getLoadParameters(PositionKey key) {
        return new Object[] { key.getId() };
    }

    @Override
    protected String getLoadQuery() {
        return "select id, amount, security_id from " + getTableName() + " where id = ?";
    }

    @Override
    protected String getTableName() {
        return "position";
    }
}
