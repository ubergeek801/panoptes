package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * SerializerUtilTest tests the functionality of SerializerUtil.
 *
 * @author jeremy
 */
public class SerializerUtilTest {
    /**
     * Tests that jsonToAttributes() behaves as expected.
     */
    @Test
    public void testJsonToAttributes() throws Exception {
        // ensure that TestUtil is loaded and thus initializes SecurityAttributes
        TestUtil.testSecurityProvider();

        String json = "{\"cusip\":\"0MV4CFXX\",\"yield\":2.60,\"ratingValue\":99.1,"
                + "\"maturityDate\":\"2019-07-31\"}";
        Map<SecurityAttribute<?>, ? super Object> map = SerializerUtil.jsonToAttributes(json);
        assertEquals(4, map.size(), "unexpected map size");
        assertEquals("0MV4CFXX", map.get(TestUtil.cusip), "unexpected value for cusip");
        assertEquals(new BigDecimal("2.60"), map.get(TestUtil.yield), "unexpected value for yield");
        assertEquals(99.1, (double)map.get(TestUtil.ratingValue), TestUtil.EPSILON,
                "unexpected value for ratingValue");
        assertEquals(LocalDate.of(2019, 7, 31), map.get(TestUtil.maturityDate),
                "unexpected value for maturityDate");

        String output = SerializerUtil.attributesToJson(map);

        // note the reordered keys which unfortunately makes this test slightly brittle
        assertEquals(
                "{\"cusip\":\"0MV4CFXX\",\"maturityDate\":\"2019-07-31\","
                        + "\"yield\":2.60,\"ratingValue\":99.1}",
                output.toString(), "unexpected JSON");
    }
}
