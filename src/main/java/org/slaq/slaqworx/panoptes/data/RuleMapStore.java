package org.slaq.slaqworx.panoptes.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * RuleMapStore is a Hazelcast MapStore that provides Rule persistence services.
 *
 * @author jeremy
 */
@Service
public class RuleMapStore extends HazelcastMapStore<RuleKey, Rule> {
    private static final long serialVersionUID = 1L;

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
        // FIXME implement delete()
    }

    @Override
    public Rule mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString(1);
        String description = rs.getString(2);

        // FIXME implement rule polymorphism
        return new ConcentrationRule(new RuleKey(id), description, null, null, null, null);
    }

    @Override
    public void store(RuleKey key, Rule value) {
        // FIXME implement store()
    }

    @Override
    protected String getIdColumnNames() {
        return "id, version";
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
        return "select id, description from " + getTableName() + " where id = ?";
    }

    @Override
    protected String getTableName() {
        return "rule";
    }
}
