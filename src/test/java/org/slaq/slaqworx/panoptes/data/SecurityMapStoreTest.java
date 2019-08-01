package org.slaq.slaqworx.panoptes.data;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * SecurityMapStoreTest tests the functionality of the SecurityMapStore.
 *
 * @author jeremy
 */
public class SecurityMapStoreTest {
    @Test
    public void testSerialize() throws Exception {
        // ensure that TestUtil is loaded and thus initializes SecurityAttributes
        TestUtil.testSecurityProvider();

        SecurityMapStore mapStore = new SecurityMapStore(new DummyDataSource());
        String json = "{\"cusip\":\"0MV4CFXX\",\"yield\":2.6,\"ratingValue\":99.1,"
                + "\"maturityDate\":\"2019-07-31\"}";
        Map<SecurityAttribute<?>, ? super Object> map = mapStore.jsonToAttributes(json);
        assertEquals("unexpected map size", 4, map.size());
        assertEquals("unexpected value for cusip", "0MV4CFXX", map.get(TestUtil.cusip));
        assertEquals("unexpected value for yield", new BigDecimal("2.6"), map.get(TestUtil.yield));
        assertEquals("unexpected value for ratingValue", 99.1,
                (double)map.get(TestUtil.ratingValue), TestUtil.EPSILON);
        assertEquals("unexpected value for maturityDate", LocalDate.of(2019, 7, 31),
                map.get(TestUtil.maturityDate));

        String output = mapStore.attributesToJson(map);

        assertEquals("unexpected JSON", json, output.toString());
    }
}
