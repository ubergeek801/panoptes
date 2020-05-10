package org.slaq.slaqworx.panoptes.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Singleton;
import javax.transaction.Transactional;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.transaction.SynchronousTransactionManager;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.StatementContext;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;

/**
 * {@code PositionMapStore} is a Hazelcast {@code MapStore} that provides {@code Position}
 * persistence services.
 *
 * @author jeremy
 */
@Singleton
@Requires(notEnv = { Environment.TEST, "offline" })
public class PositionMapStore extends HazelcastMapStore<PositionKey, Position> {
    /**
     * Creates a new {@code PositionMapStore}. Restricted because instances of this class should be
     * created through the {@code HazelcastMapStoreFactory}.
     *
     * @param transactionManager
     *            the {@code TransactionManager} to use for {@code loadAllKeys()}
     * @param jdbi
     *            the {@code Jdbi} instance through which to access the database
     */
    protected PositionMapStore(SynchronousTransactionManager<Connection> transactionManager,
            Jdbi jdbi) {
        super(transactionManager, jdbi);
    }

    @Override
    @Transactional
    public void delete(PositionKey key) {
        getJdbi().withHandle(handle -> {
            handle.execute("delete from portfolio_position where position_id = ?", key.getId());
            return handle.execute("delete from " + getTableName() + " where id = ?", key.getId());
        });
    }

    @Override
    public Position map(ResultSet rs, StatementContext context) throws SQLException {
        String id = rs.getString(1);
        double amount = rs.getDouble(2);
        String securityId = rs.getString(3);

        return new SimplePosition(new PositionKey(id), amount, new SecurityKey(securityId));
    }

    @Override
    protected void bindValues(PreparedBatch batch, Position position) {
        batch.bind(1, position.getKey().getId());
        batch.bind(2, position.getAmount());
        batch.bind(3, position.getSecurityKey().getId());
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

    @Override
    protected String getStoreSql() {
        return "insert into " + getTableName() + " (id, amount, security_id, partition_id) values"
                + " (?, ?, ?, 0) on conflict on constraint position_pk do update set amount ="
                + " excluded.amount, security_id = excluded.security_id";
    }

    @Override
    protected String getTableName() {
        return "position";
    }
}
