package org.slaq.slaqworx.panoptes.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.transaction.SynchronousTransactionManager;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.StatementContext;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code PortfolioMapStore} is a Hazelcast {@code MapStore} that provides {@code Portfolio}
 * persistence services.
 *
 * @author jeremy
 */
@Singleton
@Requires(notEnv = { Environment.TEST, "offline" })
public class PortfolioMapStore extends HazelcastMapStore<PortfolioKey, Portfolio> {
    private final Provider<AssetCache> assetCacheProvider;

    /**
     * Creates a new {@code PortfolioMapStore}. Restricted because instances of this class should be
     * created through the {@code HazelcastMapStoreFactory}.
     *
     * @param transactionManager
     *            the {@code TransactionManager} to use for {@code loadAllKeys()}
     * @param jdbi
     *            the {@code Jdbi} instance through which to access the database
     * @param assetCacheProvider
     *            the {@code AsssetCache} from which to obtained cached data, wrapped in a
     *            {@code Provider} to avoid a circular injection dependency
     */
    protected PortfolioMapStore(SynchronousTransactionManager<Connection> transactionManager,
            Jdbi jdbi, Provider<AssetCache> assetCacheProvider) {
        super(transactionManager, jdbi);
        this.assetCacheProvider = assetCacheProvider;
    }

    @Override
    @Transactional
    public void delete(PortfolioKey key) {
        getJdbi().withHandle(handle -> {
            handle.execute("delete from portfolio_position where portfolio_id = ? and"
                    + " portfolio_version = ?", key.getId(), key.getVersion());
            handle.execute("delete from portfolio_rule where portfolio_id = ? and portfolio_version"
                    + " = ?", key.getId(), key.getVersion());
            return handle.execute("delete from " + getTableName() + " where id = ? and version = ?",
                    key.getId(), key.getVersion());
        });
    }

    @Override
    public Portfolio map(ResultSet rs, StatementContext context) throws SQLException {
        String id = rs.getString(1);
        int version = rs.getInt(2);
        String name = rs.getString(3);
        String benchmarkId = rs.getString(4);
        int benchmarkVersion = rs.getInt(5);

        return getJdbi().withHandle(handle -> {
            // get the keys for the related Positions
            Stream<PositionKey> positionKeys = handle
                    .select("select position_id from portfolio_position where portfolio_id = ? and"
                            + " portfolio_version = ?", id, version)
                    .map((posRs, ctx) -> new PositionKey(posRs.getString(1))).stream();
            Set<Position> positions = positionKeys.map(k -> assetCacheProvider.get().getPosition(k))
                    .collect(Collectors.toSet());

            // get the keys for the related Rules
            Stream<RuleKey> ruleKeys = handle
                    .select("select rule_id from portfolio_rule where portfolio_id = ? and"
                            + " portfolio_version = ?", id, version)
                    .map((ruleRs, ctx) -> new RuleKey(ruleRs.getString(1))).stream();
            Set<ConfigurableRule> rules = ruleKeys.map(k -> assetCacheProvider.get().getRule(k))
                    .collect(Collectors.toSet());

            return new Portfolio(new PortfolioKey(id, version), name, positions,
                    (benchmarkId == null ? null : new PortfolioKey(benchmarkId, benchmarkVersion)),
                    rules);
        });
    }

    @Override
    protected void bindValues(PreparedBatch batch, Portfolio portfolio) {
        PortfolioKey benchmarkKey = portfolio.getBenchmarkKey();

        batch.bind(1, portfolio.getKey().getId());
        batch.bind(2, portfolio.getKey().getVersion());
        batch.bind(3, portfolio.getName());
        batch.bind(4, benchmarkKey == null ? null : benchmarkKey.getId());
        batch.bind(5, benchmarkKey == null ? null : benchmarkKey.getVersion());
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
    protected RowMapper<PortfolioKey> getKeyMapper() {
        return (rs, rowNum) -> new PortfolioKey(rs.getString(1), rs.getInt(2));
    }

    @Override
    protected String getLoadSelect() {
        return "select id, version, name, benchmark_id, benchmark_version from " + getTableName();
    }

    @Override
    protected String getStoreSql() {
        return "insert into " + getTableName()
                + " (id, version, name, benchmark_id, benchmark_version, partition_id) values (?,"
                + " ?, ?, ?, ?, 0) on conflict on constraint portfolio_pk do update"
                + " set name = excluded.name, benchmark_id = excluded.benchmark_id,"
                + " benchmark_version = excluded.benchmark_version";
    }

    @Override
    protected String getTableName() {
        return "portfolio";
    }

    @Override
    protected void postStoreAll(Map<PortfolioKey, Portfolio> map) {
        // now that the Portfolios have been inserted, store the Position and Rule relationships

        getJdbi().withHandle(handle -> {
            for (Portfolio portfolio : map.values()) {
                PreparedBatch batch = handle.prepareBatch(
                        "insert into portfolio_position (portfolio_id, portfolio_version,"
                                + " position_id) values (?, ?, ?) on conflict on constraint"
                                + " portfolio_position_pk ignore");
                portfolio.getPositions().forEach(position -> {
                    batch.bind(1, portfolio.getKey().getId());
                    batch.bind(2, portfolio.getKey().getVersion());
                    batch.bind(3, position.getKey().getId());
                    batch.add();
                });
                batch.execute();
            }

            for (Portfolio portfolio : map.values()) {
                PreparedBatch batch = handle.prepareBatch(
                        "insert into portfolio_rule (portfolio_id, portfolio_version, rule_id)"
                                + " values (?, ?, ?) on conflict on constraint portfolio_rule_pk"
                                + " ignore");
                portfolio.getRules().forEach(rule -> {
                    batch.bind(1, portfolio.getKey().getId());
                    batch.bind(2, portfolio.getKey().getVersion());
                    batch.bind(3, rule.getKey().getId());
                    batch.add();
                });
                batch.execute();
            }

            return null;
        });
    }
}
