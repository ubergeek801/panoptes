package org.slaq.slaqworx.panoptes.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.util.SerializerUtil;

/**
 * {@code SecurityCacheStore} is an Ignite {@code CacheStore} that provides {@code Security}
 * persistence services.
 *
 * @author jeremy
 */
public class SecurityCacheStore extends IgniteCacheStore<SecurityKey, Security> {
    /**
     * Creates a new {@code SecurityCacheStore} which obtains resources from the global
     * {@code ApplicationContext}.
     *
     * @param cacheName
     *            the name of the cache served by this store
     */
    public SecurityCacheStore(String cacheName) {
        super(cacheName);
    }

    @Override
    public void delete(Object keyObject) {
        SecurityKey key = (SecurityKey)keyObject;

        getJdbcTemplate().update("delete from " + getTableName() + " where id = ?", key.getId());
    }

    @Override
    public Security mapRow(ResultSet rs, int rowNum) throws SQLException {
        /* String id = */ rs.getString(1);
        String attributes = rs.getString(2);

        return new Security(SerializerUtil.jsonToAttributes(attributes));
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
    protected String getLoadSelect() {
        return "select id, attributes from " + getTableName();
    }

    @Override
    protected String getTableName() {
        return "security";
    }

    @Override
    protected String getWriteSql() {
        return "insert into " + getTableName() + " (id, hash, attributes, partition_id) values (?,"
                + " ?, ?::json, ?) on conflict on constraint security_pk do update set hash ="
                + " excluded.hash, attributes = excluded.attributes, partition_id ="
                + " excluded.partition_id";
    }

    @Override
    protected void setValues(PreparedStatement ps, Security security) throws SQLException {
        String jsonAttributes;
        try {
            jsonAttributes = SerializerUtil.attributesToJson(security.getAttributes().asMap());
        } catch (Exception e) {
            // TODO throw a better exception
            throw new SQLException(
                    "could not serialize SecurityAttributes for " + security.getKey(), e);
        }

        ps.setString(1, security.getKey().getId());
        ps.setString(2, security.getAttributes().hash());
        ps.setString(3, jsonAttributes);
        ps.setShort(4, getPartition(security.getKey()));
    }
}
