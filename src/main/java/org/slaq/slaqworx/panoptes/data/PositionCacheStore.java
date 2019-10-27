package org.slaq.slaqworx.panoptes.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Singleton;
import javax.sql.DataSource;

import org.apache.ignite.Ignite;

import org.slaq.slaqworx.panoptes.ApplicationContextProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;

/**
 * {@code PositionCacheStore} is an Ignite {@code CacheStore} that provides {@code Position}
 * persistence services.
 *
 * @author jeremy
 */
@Singleton
public class PositionCacheStore extends IgniteCacheStore<PositionKey, Position> {
    /**
     * Creates a new {@code PositionCacheStore}.
     *
     * @param igniteInstance
     *            the {@code Ignite} instance for which to stream data
     * @param dataSource
     *            the {@code DataSource} from which to stream data
     */
    protected PositionCacheStore(Ignite igniteInstance, DataSource dataSource) {
        super(igniteInstance, dataSource, AssetCache.POSITION_CACHE_NAME);
    }

    @Override
    public void delete(Object keyObject) {
        PositionKey key = (PositionKey)keyObject;

        getJdbcTemplate().update("delete from portfolio_position where position_id = ?",
                key.getId());
        getJdbcTemplate().update("delete from " + getTableName() + " where id = ?", key.getId());
    }

    @Override
    public Position mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString(1);
        double amount = rs.getDouble(2);
        String securityId = rs.getString(3);

        return new Position(new PositionKey(id), amount,
                getAssetCache().getSecurity(new SecurityKey(securityId)));
    }

    /**
     * Obtains the {@code AssetCache} to be used to resolve references.
     */
    protected AssetCache getAssetCache() {
        return ApplicationContextProvider.getApplicationContext().getBean(AssetCache.class);
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
    protected String getLoadSelect() {
        return "select id, amount, security_id from " + getTableName();
    }

    @Override
    protected String getTableName() {
        return "position";
    }

    @Override
    protected String getWriteSql() {
        return "insert into " + getTableName()
                + " (id, amount, security_id, partition_id) values (?, ?, ?, ?) on conflict on"
                + " constraint position_pk do update set amount = excluded.amount, security_id ="
                + " excluded.security_id, partition_id = excluded.partition_id";
    }

    @Override
    protected void setValues(PreparedStatement ps, Position position) throws SQLException {
        ps.setString(1, position.getKey().getId());
        ps.setDouble(2, position.getAmount());
        ps.setString(3, position.getSecurity().getKey().getId());
        ps.setShort(4, getPartition(position.getKey()));
    }
}
