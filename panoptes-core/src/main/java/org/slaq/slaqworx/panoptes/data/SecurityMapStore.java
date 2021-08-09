package org.slaq.slaqworx.panoptes.data;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.transaction.SynchronousTransactionManager;
import io.micronaut.transaction.TransactionManager;
import jakarta.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.transaction.Transactional;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.StatementContext;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.util.SerializerUtil;

/**
 * A {@link HazelcastMapStore} that provides {@link Security} persistence services.
 *
 * @author jeremy
 */
@Singleton
@Requires(notEnv = {Environment.TEST, "offline"})
public class SecurityMapStore extends HazelcastMapStore<SecurityKey, Security> {
  /**
   * Creates a new {@link SecurityMapStore}. Restricted because instances of this class should be
   * created through the {@link HazelcastMapStoreFactory}.
   *
   * @param transactionManager
   *     the {@link TransactionManager} to use for {@code loadAllKeys()}
   * @param jdbi
   *     the {@link Jdbi} instance through which to access the database
   */
  protected SecurityMapStore(SynchronousTransactionManager<Connection> transactionManager,
      Jdbi jdbi) {
    super(transactionManager, jdbi);
  }

  @Override
  @Transactional
  public void delete(SecurityKey key) {
    getJdbi().withHandle(
        handle -> handle.execute("delete from " + getTableName() + " where id = ?", key.id()));
  }

  @Override
  public Security map(ResultSet rs, StatementContext context) throws SQLException {
    /* String id = */
    rs.getString(1);
    String attributes = rs.getString(2);

    return new Security(SerializerUtil.jsonToAttributes(attributes));
  }

  @Override
  protected void bindValues(PreparedBatch batch, Security security) {
    String jsonAttributes;
    try {
      jsonAttributes = SerializerUtil.attributesToJson(security.getAttributes().asMap());
    } catch (Exception e) {
      // TODO throw a better exception
      throw new RuntimeException("could not serialize SecurityAttributes for " + security.getKey(),
          e);
    }

    batch.bind(1, security.getKey().id());
    batch.bind(2, security.getAttributes().hash());
    batch.bind(3, jsonAttributes);
  }

  @Override
  protected String[] getKeyColumnNames() {
    return new String[] {"id"};
  }

  @Override
  protected Object[] getKeyComponents(SecurityKey key) {
    return new Object[] {key.id()};
  }

  @Override
  protected RowMapper<SecurityKey> getKeyMapper() {
    return (rs, context) -> new SecurityKey(rs.getString(1));
  }

  @Override
  protected String getLoadSelect() {
    return "select id, attributes from " + getTableName();
  }

  @Override
  protected String getStoreSql() {
    return "insert into " + getTableName() + """
         (id, hash, attributes, partition_id)
         values (?, ?, ?::json, 0)
         on conflict on constraint security_pk do update
          set hash = excluded.hash, attributes = excluded.attributes
        """;
  }

  @Override
  protected String getTableName() {
    return "security";
  }
}
