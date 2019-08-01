package org.slaq.slaqworx.panoptes.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

@Service
public class PositionMapStore extends HazelcastMapStore<PositionKey, Position> {
    private static final long serialVersionUID = 1L;

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
        int version = rs.getInt(2);
        double amount = rs.getDouble(3);
        String securityId = rs.getString(4);

        return new Position(new PositionKey(id, version), amount, new SecurityKey(securityId));
    }

    @Override
    public void store(PositionKey key, Position position) {
        // FIXME update to handle update if necessary
        getJdbcTemplate().update(
                "insert into " + getTableName()
                        + " (id, version, amount, security_id) values (?, ?, ?, ?)",
                key.getId(), key.getVersion(), position.getAmount(), position.getSecurityKey());
    }

    @Override
    protected String getIdColumnNames() {
        return "id, version";
    }

    @Override
    protected RowMapper<PositionKey> getKeyMapper() {
        return (rs, rowNum) -> new PositionKey(rs.getString(1), rs.getInt(2));
    }

    @Override
    protected Object[] getLoadParameters(PositionKey key) {
        return new Object[] { key.getId(), key.getVersion() };
    }

    @Override
    protected String getLoadQuery() {
        return "select id, version, amount, security_id from " + getTableName()
                + " where id = ? and version = ?";
    }

    @Override
    protected String getTableName() {
        return "position";
    }
}
