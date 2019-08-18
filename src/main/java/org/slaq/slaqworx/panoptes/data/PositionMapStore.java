package org.slaq.slaqworx.panoptes.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import io.micronaut.context.ApplicationContext;

import org.springframework.jdbc.core.RowMapper;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.PortfolioCache;

/**
 * {@code PositionMapStore} is a Hazelcast {@code MapStore} that provides {@code Position}
 * persistence services.
 *
 * @author jeremy
 */
public class PositionMapStore extends HazelcastMapStore<PositionKey, Position> {
    private ApplicationContext applicationContext;

    /**
     * Creates a new PositionMapStore. Restricted because instances of this class should be created
     * through the {@code HazelcastMapStoreFactory}.
     *
     * @param applicationContext
     *            the {@code ApplicationConext} from which to resolve dependent {@code Bean}s
     * @param dataSource
     *            the {@code DataSource} through which to access the database
     */
    protected PositionMapStore(ApplicationContext applicationContext, DataSource dataSource) {
        super(dataSource);
        this.applicationContext = applicationContext;
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

        return new Position(new PositionKey(id), amount,
                getPortfolioCache().getSecurity(new SecurityKey(securityId)));
    }

    @Override
    public void store(PositionKey key, Position position) {
        getJdbcTemplate().update(
                "insert into " + getTableName() + " (id, amount, security_id) values (?, ?, ?)"
                        + " on conflict on constraint position_pk do update"
                        + " set amount = excluded.amount, security_id = excluded.security_id",
                key.getId(), position.getAmount(), position.getSecurity().getKey().getId());
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

    /**
     * Obtains the {@code PortfolioCache} to be used to resolve references. Lazily obtained to avoid
     * a circular injection dependency.
     *
     * @param portfolioCache
     *            the {@code PortfolioCache} to use to obtain data
     */
    protected PortfolioCache getPortfolioCache() {
        return applicationContext.getBean(PortfolioCache.class);
    }

    @Override
    protected String getTableName() {
        return "position";
    }
}
