package org.slaq.slaqworx.panoptes.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * PortfolioMapStore is a Hazelcast MapStore that provides Portfolio persistence services.
 *
 * @author jeremy
 */
@Service
public class PortfolioMapStore extends HazelcastMapStore<PortfolioKey, Portfolio> {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new PortfolioMapStore. Restricted because instances of this class should be created
     * through Spring.
     *
     * @param dataSource
     *            the DataSource through which to access the database
     */
    protected PortfolioMapStore(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void delete(PortfolioKey key) {
        // FIXME implement delete()
    }

    @Override
    public Portfolio mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString(1);
        int version = rs.getInt(2);
        String name = rs.getString(3);
        String benchmarkId = rs.getString(4);
        int benchmarkVersion = rs.getInt(5);

        // FIXME include positions and rules
        return new Portfolio(new PortfolioKey(id, version), name, Collections.emptySet(),
                (benchmarkId == null ? null : new PortfolioKey(benchmarkId, benchmarkVersion)),
                null);
    }

    @Override
    public void store(PortfolioKey key, Portfolio portfolio) {
        PortfolioKey benchmarkKey = portfolio.getBenchmarkKey();
        getJdbcTemplate().update("insert into " + getTableName()
                + " (id, version, name, benchmark_id, benchmark_version) values (?, ?, ?, ?, ?)"
                + " on conflict on constraint portfolio_pk do update"
                + " set name = excluded.name, benchmark_id = excluded.benchmark_id,"
                + " benchmark_version = excluded.benchmark_version", key.getId(), key.getVersion(),
                portfolio.getName(), benchmarkKey == null ? null : benchmarkKey.getId(),
                benchmarkKey == null ? null : benchmarkKey.getVersion());
        getJdbcTemplate().update(
                "delete from portfolio_position where portfolio_id = ? and portfolio_version = ?",
                key.getId(), key.getVersion());
        portfolio.getPositions().forEach(p -> getJdbcTemplate().update(
                "insert into portfolio_position (portfolio_id, portfolio_version, position_id)"
                        + " values (?, ?, ?)",
                key.getId(), key.getVersion(), p.getKey().getId()));
    }

    @Override
    protected String getIdColumnNames() {
        return "id, version";
    }

    @Override
    protected RowMapper<PortfolioKey> getKeyMapper() {
        return (rs, rowNum) -> new PortfolioKey(rs.getString(1), rs.getInt(2));
    }

    @Override
    protected Object[] getLoadParameters(PortfolioKey key) {
        return new Object[] { key.getId(), key.getVersion() };
    }

    @Override
    protected String getLoadQuery() {
        return "select id, version, name, benchmark_id, benchmark_version from " + getTableName()
                + " where id = ? and version = ?";
    }

    @Override
    protected String getTableName() {
        return "portfolio";
    }
}
