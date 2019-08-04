package org.slaq.slaqworx.panoptes.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.serializer.SerializerUtil;

/**
 * SecurityMapStore is a Hazelcast MapStore that provides Security persistence services.
 *
 * @author jeremy
 */
@Service
public class SecurityMapStore extends HazelcastMapStore<SecurityKey, Security> {
    private static final long serialVersionUID = 1L;

    /**
     * Serializes the given SecurityAttributes values to JSON.
     *
     * @param attributes
     *            the SecurityAttributes to be serialized
     * @return a JSON representation of the SecurityAttributes
     * @throws JsonProcessingException
     *             if the attributes could not be serialized
     */
    public static String attributesToJson(Map<SecurityAttribute<?>, ? super Object> attributes)
            throws JsonProcessingException {
        return SerializerUtil.defaultJsonMapper().writeValueAsString(attributes);
    }

    /**
     * Deserializes the given JSON to a Map of SecurityAttribute values.
     *
     * @param jsonAttributes
     * @return a Map of SecurityAttribute to its value
     */
    public static Map<SecurityAttribute<?>, ? super Object>
            jsonToAttributes(String jsonAttributes) {
        // first let the JSON parser do the best it can, but it will default some types incorrectly
        // (e.g. Double when we want BigDecimal)

        TypeReference<Map<SecurityAttribute<?>, ? super Object>> attributeMapRef =
                new TypeReference<>() {
                    // nothing to do
                };
        Map<SecurityAttribute<?>, ? super Object> jsonMap;
        try {
            jsonMap = SerializerUtil.defaultJsonMapper().readValue(jsonAttributes, attributeMapRef);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not deserialize SecurityAttributes", e);
        }

        // now coerce the values into their expected types based on the corresponding
        // SecurityAttributes
        return jsonMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),
                e -> SerializerUtil.coerce(e.getKey(), e.getValue())));
    }

    /**
     * Creates a new SecurityMapStore. Restricted because instances of this class should be created
     * through Spring.
     *
     * @param dataSource
     *            the DataSource through which to access the database
     */
    protected SecurityMapStore(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void delete(SecurityKey key) {
        getJdbcTemplate().update("delete from " + getTableName() + " where id = ?", key.getId());
    }

    @Override
    public Security mapRow(ResultSet rs, int rowNum) throws SQLException {
        /* String id = */ rs.getString(1);
        String attributes = rs.getString(2);

        return new Security(jsonToAttributes(attributes));
    }

    @Override
    public void store(SecurityKey key, Security security) {
        try {
            getJdbcTemplate().update(
                    "insert into " + getTableName() + " (id, attributes) values (?, ?::json)"
                            + " on conflict on constraint security_pk"
                            + " do update set attributes = excluded.attributes",
                    key.getId(), attributesToJson(security.getAttributes()));
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not serialize SecurityAttributes for " + key, e);
        }
    }

    @Override
    protected String getIdColumnNames() {
        return "id";
    }

    @Override
    protected RowMapper<SecurityKey> getKeyMapper() {
        return (rs, rowNum) -> new SecurityKey(rs.getString(1));
    }

    @Override
    protected Object[] getLoadParameters(SecurityKey key) {
        return new Object[] { key.getId() };
    }

    @Override
    protected String getLoadQuery() {
        return "select id, attributes from " + getTableName() + " where id = ?";
    }

    @Override
    protected String getTableName() {
        return "security";
    }
}
