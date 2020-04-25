package org.slaq.slaqworx.panoptes.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Singleton;
import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;

import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.serializer.RuleSerializer;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * {@code RuleMapStore} is a Hazelcast {@code MapStore} that provides {@code Rule} persistence
 * services.
 *
 * @author jeremy
 */
@Singleton
public class RuleMapStore extends HazelcastMapStore<RuleKey, ConfigurableRule> {
    /**
     * Creates a new {@code RuleMapStore}. Restricted because instances of this class should be
     * created through the {@code ApplicationContext}.
     *
     * @param dataSource
     *            the {@code DataSource} through which to access the database
     */
    protected RuleMapStore(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void delete(RuleKey key) {
        getJdbcTemplate().update("delete from portfolio_rule where rule_id = ?", key.getId());
        getJdbcTemplate().update("delete from " + getTableName() + " where id = ?", key.getId());
    }

    @Override
    public ConfigurableRule mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString(1);
        String description = rs.getString(2);
        String ruleTypeName = rs.getString(3);
        String configuration = rs.getString(4);
        String groovyFilter = rs.getString(5);
        String classifierTypeName = rs.getString(6);
        String classifierConfiguration = rs.getString(7);

        return RuleSerializer.constructRule(id, description, ruleTypeName, configuration,
                groovyFilter, classifierTypeName, classifierConfiguration);
    }

    @Override
    protected String[] getKeyColumnNames() {
        return new String[] { "id" };
    }

    @Override
    protected Object[] getKeyComponents(RuleKey key) {
        return new Object[] { key.getId() };
    }

    @Override
    protected RowMapper<RuleKey> getKeyMapper() {
        return (rs, rowNum) -> new RuleKey(rs.getString(1));
    }

    @Override
    protected String getLoadSelect() {
        return "select id, description, type, configuration, filter, classifier_type,"
                + " classifier_configuration from " + getTableName();
    }

    @Override
    protected String getStoreSql() {
        return "insert into " + getTableName()
                + " (id, description, type, configuration, filter, classifier_type,"
                + " classifier_configuration, partition_id) values (?, ?, ?, ?::json, ?, ?,"
                + " ?::json, 0) on conflict on constraint rule_pk do update"
                + " set description = excluded.description, type = excluded.type,"
                + " configuration = excluded.configuration, filter = excluded.filter,"
                + " classifier_type = excluded.classifier_type,"
                + " classifier_configuration = excluded.classifier_configuration";
    }

    @Override
    protected String getTableName() {
        return "rule";
    }

    @Override
    protected void setValues(PreparedStatement ps, ConfigurableRule rule) throws SQLException {
        String classifierType;
        String classifierConfiguration;
        EvaluationGroupClassifier classifier = rule.getGroupClassifier();
        if (classifier == null) {
            classifierType = null;
            classifierConfiguration = null;
        } else {
            classifierType = classifier.getClass().getName();
            classifierConfiguration = (classifier instanceof JsonConfigurable
                    ? ((JsonConfigurable)classifier).getJsonConfiguration()
                    : null);
        }

        ps.setString(1, rule.getKey().getId());
        ps.setString(2, rule.getDescription());
        ps.setString(3, rule.getClass().getName());
        ps.setString(4, rule.getJsonConfiguration());
        ps.setString(5, rule.getGroovyFilter());
        ps.setString(6, classifierType);
        ps.setString(7, classifierConfiguration);
    }
}
