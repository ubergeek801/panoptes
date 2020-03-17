package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.util.SerializerUtil;

/**
 * {@code SecuritySerializerTest} tests the functionality of the {@code SecuritySerializer}.
 *
 * @author jeremy
 */
public class SecuritySerializerTest {
    /**
     * Tests that (de)serialization works with the values specified in a test file.
     */
    @Test
    public void testMoreSerialization() throws Exception {
        SecuritySerializer serializer = new SecuritySerializer();

        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader()
                .getResourceAsStream("SecurityAttributeTestValues.txt")));
        String json;
        while ((json = reader.readLine()) != null) {
            Map<SecurityAttribute<?>, Object> attributes = SerializerUtil.jsonToAttributes(json);
            Security fromJson = new Security(attributes);
            byte[] serialized = serializer.write(fromJson);
            Security fromSerialized = serializer.read(serialized);
            assertEquals(fromJson, fromSerialized, "JSON and serialized versions should be equal");
            serialized = serializer.write(fromSerialized);
            fromSerialized = serializer.read(serialized);
            assertEquals(fromJson, fromSerialized, "JSON and serialized versions should be equal");
        }
    }

    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        SecuritySerializer serializer = new SecuritySerializer();

        Map<SecurityAttribute<?>, ? super Object> attributes =
                SecurityAttribute.mapOf(SecurityAttribute.isin, "dummy", SecurityAttribute.country,
                        "US", SecurityAttribute.coupon, 4d, SecurityAttribute.currency, "USD",
                        SecurityAttribute.maturityDate, LocalDate.now(), SecurityAttribute.duration,
                        3.1, SecurityAttribute.price, 99d);
        Security security = new Security(attributes);

        byte[] buffer = serializer.write(security);
        Security deserialized = serializer.read(buffer);

        // because hash() is a proxy for equality of the attribute contents, equality of hash()
        // suffices for our purposes
        assertEquals(security.getAttributes().hash(), deserialized.getAttributes().hash(),
                "deserialized value and original value should have equal hash()");
    }
}
