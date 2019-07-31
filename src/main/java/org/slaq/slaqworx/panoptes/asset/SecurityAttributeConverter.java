package org.slaq.slaqworx.panoptes.asset;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

/**
 * SecurityAttributeDeserializer is a Jackson MapEntryDeserializer that handles Maps of
 * SecurityAttributes.
 *
 * @author jeremy
 */
public class SecurityAttributeConverter implements
        Converter<Map<SecurityAttribute<?>, ? super Object>, Map<SecurityAttribute<?>, ? super Object>> {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityAttributeConverter.class);

    @Override
    public Map<SecurityAttribute<?>, ? super Object>
            convert(Map<SecurityAttribute<?>, ? super Object> inputMap) {
        System.err.println("map was called");
        ObjectMapper mapper = new ObjectMapper();

        HashMap<SecurityAttribute<?>, ? super Object> outputMap = new HashMap<>(inputMap.size());
        inputMap.forEach((a, v) -> {
            Object value;
            if (v == null) {
                value = null;
            } else {
                // use the expected data type for the SecurityAttribute to attempt to parse the
                // value
                Class<?> attributeType = a.getType();
                if (attributeType == null) {
                    LOG.warn("type not mapped for SecurityAttribute {} (value {}); "
                            + "using JSON default {}", a.getName(), v, v.getClass());
                    value = v;
                } else {
                    value = mapper.convertValue(v, attributeType);
                }
            }
            outputMap.put(a, value);
        });

        return outputMap;
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructMapType(Map.class, SecurityAttribute.class, Object.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructMapType(Map.class, SecurityAttribute.class, Object.class);
    }
}
