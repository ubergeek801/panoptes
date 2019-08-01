package org.slaq.slaqworx.panoptes.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

@Service
public class PortfolioMapStore extends HazelcastMapStore<PortfolioKey, Portfolio> {
    private static final long serialVersionUID = 1L;

    protected PortfolioMapStore(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void delete(PortfolioKey key) {
        // FIXME implement delete()
    }

    @Override
    public Portfolio mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString(1);
        int version = rs.getInt(2);
        String name = rs.getString(3);
        String benchmarkId = rs.getString(4);
        int benchmarkVersion = rs.getInt(5);

        // FIXME include benchmark, positions and rules
        return new Portfolio(new PortfolioKey(id, version), Collections.emptySet());
    }

    @Override
    public void store(PortfolioKey key, Portfolio value) {
        // FIXME implement store()
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

    @Override
    protected String getTableName() {
        return "portfolio";
    }
}
