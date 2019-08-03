package org.slaq.slaqworx.panoptes.data;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.MaterializedRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * RuleMapStore is a Hazelcast MapStore that provides Rule persistence services.
 *
 * @author jeremy
 */
@Service
public class RuleMapStore extends HazelcastMapStore<RuleKey, MaterializedRule>
        implements ApplicationContextAware {
    private static final long serialVersionUID = 1L;

    private ApplicationContext applicationContext;

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
        PortfolioCache securityProvider = applicationContext.getBean(PortfolioCache.class);

        String id = rs.getString(1);
        String description = rs.getString(2);
        String ruleTypeName = rs.getString(3);
        String configuration = rs.getString(4);
        String groovyFilter = rs.getString(5);
        String classifierTypeName = rs.getString(6);
        String classifierConfiguration = rs.getString(7);

        Class<EvaluationGroupClassifier> classifierType =
                resolveClass(classifierTypeName, "classifier", id, description);
        EvaluationGroupClassifier classifier;
        if (classifierType == null) {
            classifier = null;
        } else {
            try {
                Method fromJsonMethod =
                        classifierType.getMethod("fromJson", String.class, SecurityProvider.class);
                classifier = (EvaluationGroupClassifier)fromJsonMethod.invoke(null,
                        classifierConfiguration, securityProvider);
            } catch (Exception e) {
                // TODO throw a better exception
                throw new RuntimeException("could not instantiate classifier class " + ruleTypeName
                        + " for rule " + id + "(" + description + ")");
            }
        }

        Class<MaterializedRule> ruleType = resolveClass(ruleTypeName, "rule", id, description);
        MaterializedRule rule;
        try {
            Method fromJsonMethod = ruleType.getMethod("fromJson", String.class,
                    SecurityProvider.class, RuleKey.class, String.class, String.class,
                    EvaluationGroupClassifier.class);
            rule = (MaterializedRule)fromJsonMethod.invoke(null, configuration, securityProvider,
                    new RuleKey(id), description, groovyFilter, classifier);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not instantiate rule class " + ruleTypeName
                    + " for rule " + id + "(" + description + ")");
        }

        return rule;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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
                        + " classifier_configuration) values (?, ?, ?, ?, ?, ?, ?)"
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
    protected String getIdColumnNames() {
        return "id";
    }

    @Override
    protected RowMapper<RuleKey> getKeyMapper() {
        return (rs, rowNum) -> new RuleKey(rs.getString(1));
    }

    @Override
    protected Object[] getLoadParameters(RuleKey key) {
        return new Object[] { key.getId() };
    }

    @Override
    protected String getLoadQuery() {
        return "select id, description, type, configuration, filter, classifier_type,"
                + " classifier_configuration from " + getTableName() + " where id = ?";
    }

    @Override
    protected String getTableName() {
        return "rule";
    }

    /**
     * Resolves the Class with the given name.
     *
     * @param <T>
     *            the expected type of the returned Class
     * @param className
     *            the name of the Class to resolve
     * @param function
     *            the function that the class serves (for logging purposes)
     * @param ruleId
     *            the rule ID for which the Class is being instantiated (for logging purposes)
     * @param ruleDescription
     *            the rule description for which the Class is being instantiated (for logging
     *            purposes)
     * @return the requested Class, or null if the given class name was null
     */
    protected <T> Class<T> resolveClass(String className, String function, String ruleId,
            String ruleDescription) {
        if (className == null) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>)Class.forName(className);

            return clazz;
        } catch (ClassNotFoundException e) {
            // TODO throw a better exception
            throw new RuntimeException("could not find " + function + " class " + className
                    + " for rule " + ruleId + "(" + ruleDescription + ")");
        }
    }
}
