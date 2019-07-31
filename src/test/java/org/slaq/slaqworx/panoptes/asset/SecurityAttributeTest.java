package org.slaq.slaqworx.panoptes.asset;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        String json = "{\"cusip\":\"0MV4CFXX\",\"yield\":2.6,\"ratingValue\":99.1}";

        // ensure that TestUtil is loaded and thus initializes SecurityAttributes
        TestUtil.testSecurityProvider();

        ObjectMapper mapper = new ObjectMapper();

        TypeReference<HashMap<SecurityAttribute<?>, ? super Object>> typeRef =
                new TypeReference<>() {
                    // trivial derivation
                };

        Map<SecurityAttribute<?>, ? super Object> map = mapper.readValue(json, typeRef);
        assertEquals("unexpected map size", 3, map.size());
        assertEquals("unexpected value for cusip", "0MV4CFXX", map.get(TestUtil.cusip));
        // FIXME coercion to BigDecimal is currently broken; not even sure how to test properly
        // assertEquals("unexpected value for yield", new BigDecimal("2.6"),
        // map.get(TestUtil.yield));
        assertEquals("unexpected value for ratingValue", 99.1,
                (double)map.get(TestUtil.ratingValue), TestUtil.EPSILON);

        StringWriter output = new StringWriter();
        mapper.writeValue(output, map);

        assertEquals("unexpected JSON", json, output.toString());
    }
}
