package org.slaq.slaqworx.panoptes.asset;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;

import org.slaq.slaqworx.panoptes.TestUtil;

/**
 * SecurityAttributeTest tests the functionality of SecurityAttribute.
 *
 * @author jeremy
 */
public class SecurityAttributeTest {
    /**
     * Tests that SecurityAttributes can be deserialized from JSON and serialized to JSON.
     */
    @Test
    public void testDeserialize() throws Exception {
        // add and register the SecurityAttributeDeserializer to the Jackson module
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Map.class,
                new StdDelegatingSerializer(new SecurityAttributeConverter()));
        ObjectMapper mapper = new ObjectMapper().registerModule(simpleModule);

        String json = "{\"cusip\":\"0MV4CFXX\",\"yield\":2.6,\"ratingValue\":99.1}";
        TypeReference<HashMap<SecurityAttribute<?>, ? super Object>> typeRef =
                new TypeReference<>() {
                    // trivial derivation
                };

        Map<SecurityAttribute<?>, ? super Object> map = mapper.readValue(json, typeRef);
        assertEquals("unexpected map size", 3, map.size());
        assertEquals("unexpected value for cusip", "0MV4CFXX", map.get(TestUtil.cusip));
        assertEquals("unexpected value for yield", new BigDecimal("2.6"), map.get(TestUtil.yield));
        assertEquals("unexpected value for ratingValue", 99.1,
                (double)map.get(TestUtil.ratingValue), TestUtil.EPSILON);

        StringWriter output = new StringWriter();
        mapper.writeValue(output, map);

        assertEquals("unexpected JSON", json, output.toString());
    }
}
