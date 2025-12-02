package org.slaq.slaqworx.panoptes.data;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.transaction.SynchronousTransactionManager;
import io.micronaut.transaction.TransactionManager;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.StatementContext;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.serializer.RuleSerializer;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * A {@link HazelcastMapStore} that provides {@link Rule} persistence services.
 *
 * @author jeremy
 */
@Singleton
@Requires(notEnv = {Environment.TEST, "offline"})
public class RuleMapStore extends HazelcastMapStore<RuleKey, ConfigurableRule> {
  /**
   * Creates a new {@link RuleMapStore}. Restricted because instances of this class should be
   * created through the {@link HazelcastMapStoreFactory}.
   *
   * @param transactionManager the {@link TransactionManager} to use for {@code loadAllKeys()}
   * @param jdbi the {@link Jdbi} instance through which to access the database
   */
  protected RuleMapStore(SynchronousTransactionManager<Connection> transactionManager, Jdbi jdbi) {
    super(transactionManager, jdbi);
  }

  @Override
  @Transactional
  public void delete(RuleKey key) {
    getJdbi()
        .withHandle(
            handle -> {
              handle.execute("delete from portfolio_rule where rule_id = ?", key.id());
              return handle.execute("delete from " + getTableName() + " where id = ?", key.id());
            });
  }

  @Override
  public ConfigurableRule map(ResultSet rs, StatementContext context) throws SQLException {
    String id = rs.getString(1);
    String description = rs.getString(2);
    String ruleTypeName = rs.getString(3);
    String configuration = rs.getString(4);
    String groovyFilter = rs.getString(5);
    String classifierTypeName = rs.getString(6);
    String classifierConfiguration = rs.getString(7);

    return RuleSerializer.constructRule(
        id,
        description,
        ruleTypeName,
        configuration,
        groovyFilter,
        classifierTypeName,
        classifierConfiguration);
  }

  @Override
  protected void bindValues(PreparedBatch batch, ConfigurableRule rule) {
    String classifierType;
    String classifierConfiguration;
    EvaluationGroupClassifier classifier = rule.getGroupClassifier();
    classifierType = classifier.getClass().getName();
    classifierConfiguration =
        (classifier instanceof JsonConfigurable
            ? ((JsonConfigurable) classifier).getJsonConfiguration()
            : null);

    batch.bind(1, rule.getKey().id());
    batch.bind(2, rule.getDescription());
    batch.bind(3, rule.getClass().getName());
    batch.bind(4, rule.getJsonConfiguration());
    batch.bind(5, rule.getGroovyFilter());
    batch.bind(6, classifierType);
    batch.bind(7, classifierConfiguration);
  }

  @Override
  protected String[] getKeyColumnNames() {
    return new String[] {"id"};
  }

  @Override
  protected Object[] getKeyComponents(RuleKey key) {
    return new Object[] {key.id()};
  }

  @Override
  protected RowMapper<RuleKey> getKeyMapper() {
    return (rs, rowNum) -> new RuleKey(rs.getString(1));
  }

  @Override
  protected String getLoadSelect() {
    return "select id, description, type, configuration, filter, classifier_type,"
        + " classifier_configuration from "
        + getTableName();
  }

  @Override
  protected String getStoreSql() {
    return "insert into "
        + getTableName()
        +
"""
 (id, description, type, configuration, filter, classifier_type, classifier_configuration,
   partition_id)
 values (?, ?, ?, ?::json, ?, ?, ?::json, 0)
 on conflict on constraint rule_pk do update
  set description = excluded.description, type = excluded.type,
    configuration = excluded.configuration, filter = excluded.filter,
    classifier_type = excluded.classifier_type,
    classifier_configuration = excluded.classifier_configuration
""";
  }

  @Override
  protected String getTableName() {
    return "rule";
  }
}
