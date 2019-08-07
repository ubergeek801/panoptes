package org.slaq.slaqworx.panoptes.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.MaterializedRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.serializer.RuleSerializer;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * RuleMapStore is a Hazelcast MapStore that provides Rule persistence services.
 *
 * @author jeremy
 */
@Service
public class RuleMapStore extends HazelcastMapStore<RuleKey, MaterializedRule> {
    /**
     * Creates a new RuleMapStore. Restricted because instances of this class should be created
     * through Spring.
     *
     * @param dataSource
     *            the DataSource through which to access the database
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
    public MaterializedRule mapRow(ResultSet rs, int rowNum) throws SQLException {
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
    public void store(RuleKey key, MaterializedRule rule) {
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

        getJdbcTemplate().update(
                "insert into " + getTableName()
                        + " (id, description, type, configuration, filter, classifier_type,"
                        + " classifier_configuration) values (?, ?, ?, ?::json, ?, ?, ?::json)"
                        + " on conflict on constraint rule_pk do update"
                        + " set description = excluded.description, type = excluded.type,"
                        + " configuration = excluded.configuration, filter = excluded.filter,"
                        + " classifier_type = excluded.classifier_type,"
                        + " classifier_configuration = excluded.classifier_configuration",
                key.getId(), rule.getDescription(), rule.getClass().getName(),
                rule.getJsonConfiguration(), rule.getGroovyFilter(), classifierType,
                classifierConfiguration);
    }

    @Override
    protected String[] getKeyColumnNames() {
        return new String[] { "id" };
    }

    @Override
    protected RowMapper<RuleKey> getKeyMapper() {
        return (rs, rowNum) -> new RuleKey(rs.getString(1));
    }

    @Override
    protected Object[] getKeyComponents(RuleKey key) {
        return new Object[] { key.getId() };
    }

    @Override
    protected String getLoadSelect() {
        return "select id, description, type, configuration, filter, classifier_type,"
                + " classifier_configuration from " + getTableName();
    }

    @Override
    protected String getTableName() {
        return "rule";
    }
}
