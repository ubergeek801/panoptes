package org.slaq.slaqworx.panoptes.serializer;

import java.math.BigDecimal;
import java.time.temporal.Temporal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

public class SerializerUtil {
    private static final ObjectMapper defaultJsonMapper =
            new ObjectMapper().registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    /**
     * Attempts to coerce the given value into the type specified by the given SecurityAttribute.
     *
     * @param attribute
     *            the attribute to coerce the value to
     * @param value
     *            the value to be coerced
     * @return the coerced value
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

    public static ObjectMapper defaultJsonMapper() {
        return defaultJsonMapper;
    }
}
