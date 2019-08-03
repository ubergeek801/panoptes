package org.slaq.slaqworx.panoptes.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.PositionProxy;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProxy;

/**
 * PortfolioMapStore is a Hazelcast MapStore that provides Portfolio persistence services.
 *
 * @author jeremy
 */
@Service
public class PortfolioMapStore extends HazelcastMapStore<PortfolioKey, Portfolio> {
    private static final long serialVersionUID = 1L;

    private transient ApplicationContext applicationContext;

    /**
     * Creates a new PortfolioMapStore. Restricted because instances of this class should be created
     * through Spring.
     *
     * @param applicationContext
     *            the ApplicationConext from which to resolve dependent Beans
     * @param dataSource
     *            the DataSource through which to access the database
     */
    protected PortfolioMapStore(ApplicationContext applicationContext, DataSource dataSource) {
        super(dataSource);
        this.applicationContext = applicationContext;
    }

    @Override
    public void delete(PortfolioKey key) {
        getJdbcTemplate().update(
                "delete from portfolio_position where portfolio_id = ? and portfolio_version = ?",
                key.getId(), key.getVersion());
        getJdbcTemplate().update(
                "delete from portfolio_rule where portfolio_id = ? and portfolio_version = ?",
                key.getId(), key.getVersion());
        getJdbcTemplate().update("delete from " + getTableName() + " where id = ? and version = ?",
                key.getId(), key.getVersion());
    }

    @Override
    public Portfolio mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString(1);
        int version = rs.getInt(2);
        String name = rs.getString(3);
        String benchmarkId = rs.getString(4);
        int benchmarkVersion = rs.getInt(5);

        // get the keys for the related Positions
        List<PositionKey> positionKeys = getJdbcTemplate().query(
                "select position_id from portfolio_position"
                        + " where portfolio_id = ? and portfolio_version = ?",
                new Object[] { id, version },
                (RowMapper<PositionKey>)(rsPos, rowNumPos) -> new PositionKey(rsPos.getString(1)));
        // some unfortunate copying of Collections to Sets
        Set<Position> positions = positionKeys.stream()
                .map(k -> new PositionProxy(k, getPortfolioCache())).collect(Collectors.toSet());

        // get the keys for the related Rules
        List<RuleKey> ruleKeys = getJdbcTemplate().query(
                "select rule_id from portfolio_rule"
                        + " where portfolio_id = ? and portfolio_version = ?",
                new Object[] { id, version },
                (RowMapper<RuleKey>)(rsPos, rowNumPos) -> new RuleKey(rsPos.getString(1)));
        Set<Rule> rules = ruleKeys.stream().map(k -> new RuleProxy(k, getPortfolioCache()))
                .collect(Collectors.toSet());

        return new Portfolio(new PortfolioKey(id, version), name, positions,
                (benchmarkId == null ? null : new PortfolioKey(benchmarkId, benchmarkVersion)),
                rules);
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

        getJdbcTemplate().update(
                "delete from portfolio_rule where portfolio_id = ? and portfolio_version = ?",
                key.getId(), key.getVersion());
        portfolio.getRuleKeys()
                .forEach(r -> getJdbcTemplate().update(
                        "insert into portfolio_rule (portfolio_id, portfolio_version, rule_id)"
                                + " values (?, ?, ?)",
                        key.getId(), key.getVersion(), r.getId()));
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

    /**
     * Obtains the PortfolioCache to be used to resolve references. Lazily obtained to avoid a
     * circular injection dependency.
     *
     * @param portfolioCache
     *            the PortfolioCache to use to obtain data
     */
    protected PortfolioCache getPortfolioCache() {
        return applicationContext.getBean(PortfolioCache.class);
    }

    @Override
    protected String getTableName() {
        return "portfolio";
    }
}
