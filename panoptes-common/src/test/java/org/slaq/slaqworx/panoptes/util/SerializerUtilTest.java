package org.slaq.slaqworx.panoptes.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import io.micronaut.core.serialize.JdkSerializer;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * {@code SerializerUtilTest} tests the functionality of {@code SerializerUtil}.
 *
 * @author jeremy
 */
public class SerializerUtilTest {
    /**
     * Tests that {@code coerce()} behaves as expected.
     */
    @Test
    public void testCocerce() {
        SecurityAttribute<String> stringAttribute =
                SecurityAttribute.of("stringAttribute", 101, String.class, null);

        String coercedString = SerializerUtil.coerce(stringAttribute, "aString");
        assertEquals("aString", coercedString, "coerced String should equal original");

        coercedString = SerializerUtil.coerce(stringAttribute, null);
        assertNull(coercedString, "coerced null should be null");

        coercedString = SerializerUtil.coerce(stringAttribute, 31337);
        assertEquals("31337", coercedString, "coerced int should equal stringified value");

        SecurityAttribute<BigDecimal> bigDecimalAttribute =
                SecurityAttribute.of("bigDecimalAttribute", 102, BigDecimal.class, null);

        BigDecimal coercedBigDecimal = SerializerUtil.coerce(bigDecimalAttribute, "31337");
        assertEquals(new BigDecimal("31337"), coercedBigDecimal,
                "coerced String should equal converted BigDecimal");

        coercedBigDecimal = SerializerUtil.coerce(bigDecimalAttribute, "31337.00");
        assertEquals(new BigDecimal("31337.00"), coercedBigDecimal,
                "coerced String should equal converted BigDecimal");

        coercedBigDecimal = SerializerUtil.coerce(bigDecimalAttribute, 31337);
        assertEquals(BigDecimal.valueOf(31337), coercedBigDecimal,
                "coerced int should equal converted BigDecimal");

        coercedBigDecimal = SerializerUtil.coerce(bigDecimalAttribute, 31337L);
        assertEquals(BigDecimal.valueOf(31337L), coercedBigDecimal,
                "coerced long should equal converted BigDecimal");

        coercedBigDecimal = SerializerUtil.coerce(bigDecimalAttribute, 31337f);
        assertEquals(BigDecimal.valueOf(31337f), coercedBigDecimal,
                "coerced float should equal converted BigDecimal");

        coercedBigDecimal = SerializerUtil.coerce(bigDecimalAttribute, 31337d);
        assertEquals(BigDecimal.valueOf(31337d), coercedBigDecimal,
                "coerced double should equal converted BigDecimal");

        coercedBigDecimal = SerializerUtil.coerce(bigDecimalAttribute, 31337.1);
        assertEquals(BigDecimal.valueOf(31337.1), coercedBigDecimal,
                "coerced double should equal converted BigDecimal");

        try {
            coercedBigDecimal = SerializerUtil.coerce(bigDecimalAttribute, "bogus");
            fail("coerced bogus value should have thrown exception");
        } catch (Exception expected) {
            // ignore
        }
    }

    /**
     * Tests that {@code testDefaultJdkSerializer()} behaves as expected.
     */
    @Test
    public void testDefaultJdkSerializer() {
        JdkSerializer serializer = SerializerUtil.defaultJdkSerializer();

        assertNotNull(serializer, "should have obtained serializer");

        // quick sanity check of the serializer
        byte[] serialized = serializer.serialize("foo").get();
        String deserialized = serializer.deserialize(serialized, String.class).get();
        assertEquals("foo", deserialized, "deserialized value should equals() original");
    }

    /**
     * Tests that {@code jsonToAttributes()} behaves as expected.
     *
     * @throws Exception
     *             if an unexpected error occurs
     */
    @Test
    public void testJsonToAttributes() throws Exception {
        String json = "{\"cusip\":\"0MV4CFXX\",\"yield\":2.60,\"rating1Value\":99.1,"
                + "\"maturityDate\":\"2019-07-31\"}";
        Map<SecurityAttribute<?>, ? super Object> map = SerializerUtil.jsonToAttributes(json);
        assertEquals(4, map.size(), "unexpected map size");
        assertEquals("0MV4CFXX", map.get(SecurityAttribute.cusip), "unexpected value for cusip");
        assertEquals(2.6, (double)map.get(SecurityAttribute.yield), TestUtil.EPSILON,
                "unexpected value for yield");
        assertEquals(99.1, (double)map.get(SecurityAttribute.rating1Value), TestUtil.EPSILON,
                "unexpected value for rating1Value");
        assertEquals(LocalDate.of(2019, 7, 31), map.get(SecurityAttribute.maturityDate),
                "unexpected value for maturityDate");

        String output = SerializerUtil.attributesToJson(map);

        // note the reordered keys which unfortunately makes this test slightly brittle
        assertEquals(
                "{\"cusip\":\"0MV4CFXX\",\"maturityDate\":\"2019-07-31\","
                        + "\"yield\":2.6,\"rating1Value\":99.1}",
                output.toString(), "unexpected JSON");
    }
}
