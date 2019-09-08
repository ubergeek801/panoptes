package org.slaq.slaqworx.panoptes.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.serializer.SerializerUtil;

/**
 * {@code SecurityMapStore} is a Hazelcast {@code MapStore} that provides {@code Security}
 * persistence services.
 *
 * @author jeremy
 */
public class SecurityMapStore extends HazelcastMapStore<SecurityKey, Security> {
    /**
     * Creates a new {@code SecurityMapStore}. Restricted because instances of this class should be
     * created through the {@code HazelcastMapStoreFactory}.
     *
     * @param dataSource
     *            the {@code DataSource} through which to access the database
     */
    protected SecurityMapStore(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void delete(SecurityKey key) {
        getJdbcTemplate().update("delete from " + getTableName() + " where id = ?", key.getId());
    }

    @Override
    public Security mapRow(ResultSet rs, int rowNum) throws SQLException {
        /* String id = */ rs.getString(1);
        String attributes = rs.getString(2);

        return new Security(SerializerUtil.jsonToAttributes(attributes));
    }

    @Override
    public void store(SecurityKey key, Security security) {
        try {
            getJdbcTemplate().update("insert into " + getTableName() + " (id, hash, attributes) "
                    + "values (?, ?, ?::json) on conflict on constraint security_pk do update "
                    + "set hash = excluded.hash, attributes = excluded.attributes", key.getId(),
                    security.getAttributes().hash(),
                    SerializerUtil.attributesToJson(security.getAttributes().asMap()));
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not serialize SecurityAttributes for " + key, e);
        }
    }

    @Override
    protected String[] getKeyColumnNames() {
        return new String[] { "id" };
    }

    @Override
    protected Object[] getKeyComponents(SecurityKey key) {
        return new Object[] { key.getId() };
    }

    @Override
    protected RowMapper<SecurityKey> getKeyMapper() {
        return (rs, rowNum) -> new SecurityKey(rs.getString(1));
    }

    @Override
    protected String getLoadSelect() {
        return "select id, attributes from " + getTableName();
    }

    @Override
    protected String getTableName() {
        return "security";
    }
}
