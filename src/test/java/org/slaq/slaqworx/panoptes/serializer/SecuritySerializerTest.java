package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * SecuritySerializerTest tests the functionality of the SecuritySerializer.
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

        Map<SecurityAttribute<?>, ? super Object> attributes = Map.of(TestUtil.country, "US",
                TestUtil.coupon, new BigDecimal("4.00"), TestUtil.currency, "USD",
                TestUtil.maturityDate, LocalDate.now(), TestUtil.duration, 3.1);
        Security security = new Security(attributes);

        byte[] buffer = serializer.write(security);
        Security deserialized = serializer.read(buffer);

        // because the key is a hash of the attribute contents, equality of the key suffices for our
        // purposes
        assertEquals(security, deserialized, "deserialized value should equals() original value");
    }
}
