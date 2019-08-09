package org.slaq.slaqworx.panoptes.serializer;

import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * {@code SerializerUtil} provides utilities for performing various types of serialization
 * (particularly JSON).
 *
 * @author jeremy
 */
public class SerializerUtil {
    private static final ObjectMapper defaultJsonMapper =
            new ObjectMapper().registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
                    .configure(DeserializationFeature.USE_LONG_FOR_INTS, true);

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
        return defaultJsonMapper().writeValueAsString(attributes);
    }

    /**
     * Attempts to coerce the given value into the type specified by the given SecurityAttribute.
     *
     * @param attribute
     *            the attribute indicating the type to coerce the value to
     * @param value
     *            the value to be coerced
     * @return the coerced value
     * @throws RuntimeException
     *             if the value could not be coerced
     */
    public static Object coerce(SecurityAttribute<?> attribute, Object value) {
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
                    return SerializerUtil.defaultJsonMapper().readValue("\"" + value + "\"",
                            attributeType);
                }
            }

            // do whatever the JSON mapper would do
            return SerializerUtil.defaultJsonMapper().readValue(String.valueOf(value),
                    attributeType);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not parse value " + value, e);
        }
    }

    /**
     * Obtains a JSON {@code ObjectMapper} suitable for most purposes.
     *
     * @return an {@code ObjectMapper}
     */
    public static ObjectMapper defaultJsonMapper() {
        return defaultJsonMapper;
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
     * Creates a new {@code SerializerUtil}. Restricted to enforce class utility semantics.
     */
    private SerializerUtil() {
        // nothing to do
    }
}
