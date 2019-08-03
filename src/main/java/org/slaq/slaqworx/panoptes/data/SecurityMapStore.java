package org.slaq.slaqworx.panoptes.data;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.Temporal;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * SecurityMapStore is a Hazelcast MapStore that provides Security persistence services.
 *
 * @author jeremy
 */
@Service
public class SecurityMapStore extends HazelcastMapStore<SecurityKey, Security> {
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper jsonMapper =
            new ObjectMapper().registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

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

    /**
     * Serializes the given SecurityAttributes values to JSON.
     *
     * @param attributes
     *            the SecurityAttributes to be serialized
     * @return a JSON representation of the SecurityAttributes
     * @throws JsonProcessingException
     *             if the attributes could not be serialized
     */
    protected String attributesToJson(Map<SecurityAttribute<?>, ? super Object> attributes)
            throws JsonProcessingException {
        return jsonMapper.writeValueAsString(attributes);
    }

    /**
     * Attempts to cocerce the given value into the type specified by the given SecurityAttribute.
     *
     * @param attribute
     *            the attribute to coerce the value to
     * @param value
     *            the value to be coerced
     * @return the cocerced value
     */
    protected Object coerce(SecurityAttribute<?> attribute, Object value) {
        if (value == null) {
            return null;
        }

        Class<?> attributeType = attribute.getType();
        if (attributeType.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (attributeType == String.class) {
            return String.valueOf(value);
        }

        try {
            if (attributeType == BigDecimal.class) {
                if (value instanceof String) {
                    return new BigDecimal((String)value);
                }
                if (value instanceof Double) {
                    return BigDecimal.valueOf((Double)value);
                }
                if (value instanceof Float) {
                    return BigDecimal.valueOf((Float)value);
                }
                if (value instanceof Integer) {
                    return new BigDecimal((Integer)value);
                }
                if (value instanceof Long) {
                    return new BigDecimal((Long)value);
                }
            } else if (Temporal.class.isAssignableFrom(attributeType)) {
                // probably one of the java.time classes; give it a try
                if (value instanceof String) {
                    return jsonMapper.readValue("\"" + value + "\"", attributeType);
                }
            }
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not parse value " + value, e);
        }

        throw new IllegalArgumentException("cannot coerce value " + value + "(" + value.getClass()
                + ") to " + attributeType.getName());
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

    /**
     * Deserializes the given JSON to a Map of SecurityAttribute values.
     *
     * @param jsonAttributes
     * @return a Map of SecurityAttribute to its value
     */
    protected Map<SecurityAttribute<?>, ? super Object> jsonToAttributes(String jsonAttributes) {
        // first let the JSON parser do the best it can, but it will default some types incorrectly
        // (e.g. Double when we want BigDecimal)

        TypeReference<Map<SecurityAttribute<?>, ? super Object>> attributeMapRef =
                new TypeReference<>() {
                    // nothing to do
                };
        Map<SecurityAttribute<?>, ? super Object> jsonMap;
        try {
            jsonMap = jsonMapper.readValue(jsonAttributes, attributeMapRef);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not deserialize SecurityAttributes", e);
        }

        // now coerce the values into their expected types based on the corresponding
        // SecurityAttributes
        return jsonMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> coerce(e.getKey(), e.getValue())));
    }
}
