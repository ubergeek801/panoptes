package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.Test;

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
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        SecuritySerializer serializer = new SecuritySerializer();

        Map<SecurityAttribute<?>, ? super Object> attributes = Map.of(TestUtil.country, "US",
                TestUtil.coupon, new BigDecimal("4.0"), TestUtil.currency, "USD",
                TestUtil.maturityDate, LocalDate.now(), TestUtil.duration, 3.0);
        Security security = new Security(attributes);

        byte[] buffer = serializer.write(security);
        Security deserialized = serializer.read(buffer);

        // because the key is a hash of the attribute contents, equality of the key suffices for our
        // purposes
        assertEquals("deserialized value should equals() original value", security, deserialized);
    }
}
