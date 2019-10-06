package org.slaq.slaqworx.panoptes.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import org.slaq.slaqworx.panoptes.ApplicationContextProvider;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code PortfolioCacheStore} is an Ignite {@code CacheStore} that provides {@code Portfolio}
 * persistence services.
 *
 * @author jeremy
 */
@Singleton
public class PortfolioCacheStore extends IgniteCacheStore<PortfolioKey, Portfolio> {
    /**
     * Creates a new {@code PortfolioCacheStore} which obtains resources from the global
     * {@code ApplicationContext}.
     */
    public PortfolioCacheStore() {
        // nothing to do
    }

    @Override
    public void delete(Object keyObject) {
        PortfolioKey key = (PortfolioKey)keyObject;

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
        Set<Position> positions = positionKeys.stream().map(k -> getAssetCache().getPosition(k))
                .collect(Collectors.toSet());

        // get the keys for the related Rules
        List<RuleKey> ruleKeys = getJdbcTemplate().query(
                "select rule_id from portfolio_rule"
                        + " where portfolio_id = ? and portfolio_version = ?",
                new Object[] { id, version },
                (RowMapper<RuleKey>)(rsPos, rowNumPos) -> new RuleKey(rsPos.getString(1)));
        Set<ConfigurableRule> rules =
                ruleKeys.stream().map(k -> getAssetCache().getRule(k)).collect(Collectors.toSet());

        return new Portfolio(new PortfolioKey(id, version), name, positions,
                (benchmarkId == null ? null : new PortfolioKey(benchmarkId, benchmarkVersion)),
                rules);
    }

    /**
     * Obtains the {@code AssetCache} to be used to resolve references.
     */
    protected AssetCache getAssetCache() {
        return ApplicationContextProvider.getApplicationContext().getBean(AssetCache.class);
    }

    @Override
    protected String[] getKeyColumnNames() {
        return new String[] { "id", "version" };
    }

    @Override
    protected Object[] getKeyComponents(PortfolioKey key) {
        return new Object[] { key.getId(), key.getVersion() };
    }

    @Override
    protected String getLoadSelect() {
        return "select id, version, name, benchmark_id, benchmark_version from " + getTableName();
    }

    @Override
    protected String getTableName() {
        return "portfolio";
    }

    @Override
    protected String getWriteSql() {
        return "insert into " + getTableName()
                + " (id, version, name, benchmark_id, benchmark_version) values (?, ?, ?, ?, ?)"
                + " on conflict on constraint portfolio_pk do update"
                + " set name = excluded.name, benchmark_id = excluded.benchmark_id,"
                + " benchmark_version = excluded.benchmark_version";
    }

    @Override
    protected void postWriteAll(Map<? extends PortfolioKey, ? extends Portfolio> map) {
        // now that the Portfolios have been inserted, store the Position and Rule relationships

        for (Portfolio portfolio : map.values()) {
            Iterator<Position> positionIter = portfolio.getPositions().iterator();
            getJdbcTemplate().batchUpdate(
                    "insert into portfolio_position (portfolio_id, portfolio_version, position_id) "
                            + "values (?, ?, ?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public int getBatchSize() {
                            return (int)portfolio.getPositions().count();
                        }

                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Position position = positionIter.next();
                            ps.setString(1, portfolio.getKey().getId());
                            ps.setLong(2, portfolio.getKey().getVersion());
                            ps.setString(3, position.getKey().getId());
                        }
                    });
        }

        for (Portfolio portfolio : map.values()) {
            Iterator<Rule> ruleIter = portfolio.getRules().iterator();
            getJdbcTemplate().batchUpdate(
                    "insert into portfolio_rule (portfolio_id, portfolio_version, rule_id) "
                            + "values (?, ?, ?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public int getBatchSize() {
                            return (int)portfolio.getRules().count();
                        }

                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Rule rule = ruleIter.next();
                            ps.setString(1, portfolio.getKey().getId());
                            ps.setLong(2, portfolio.getKey().getVersion());
                            ps.setString(3, rule.getKey().getId());
                        }
                    });
        }
    }

    @Override
    protected void setValues(PreparedStatement ps, Portfolio portfolio) throws SQLException {
        PortfolioKey benchmarkKey = portfolio.getBenchmarkKey();

        ps.setString(1, portfolio.getKey().getId());
        ps.setLong(2, portfolio.getKey().getVersion());
        ps.setString(3, portfolio.getName());
        ps.setString(4, benchmarkKey == null ? null : benchmarkKey.getId());
        if (benchmarkKey == null) {
            ps.setNull(5, Types.INTEGER);
        } else {
            ps.setLong(5, benchmarkKey.getVersion());
        }
    }
}
