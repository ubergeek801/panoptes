package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;

/**
 * {@code SecurityTest} tests the functionality of {@code Security}.
 *
 * @author jeremy
 */
public class SecurityTest {
    /**
     * Tests that {@code getAttributes()} behaves as expected.
     */
    @Test
    public void testGetAttributes() {
        TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

        Security s = securityProvider.newSecurity("dummy",
                Map.of(SecurityAttribute.country, "US", SecurityAttribute.cusip, "abcde",
                        SecurityAttribute.duration, 3.1, SecurityAttribute.coupon,
                        new BigDecimal("4.00"), SecurityAttribute.maturityDate,
                        LocalDate.of(2019, 8, 5)));
        SecurityAttributes attributes = s.getAttributes();
        assertEquals("US", attributes.getValue(SecurityAttribute.country),
                "country value should have matched");
        assertEquals("abcde", attributes.getValue(SecurityAttribute.cusip),
                "cusip value should have matched");
        assertEquals(3.1, attributes.getValue(SecurityAttribute.duration), TestUtil.EPSILON,
                "duration value should have matched");
        assertEquals(new BigDecimal("4.00"), attributes.getValue(SecurityAttribute.coupon),
                "coupon value should have matched");
        assertEquals(LocalDate.of(2019, 8, 5), attributes.getValue(SecurityAttribute.maturityDate),
                "maturity date value should have matched");
    }

    /**
     * Tests that {@code Securities} are hashed in a reasonable way.
     */
    @Test
    public void testHash() {
        TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

        Security s1 = securityProvider.newSecurity("s1",
                Map.of(SecurityAttribute.country, "US", SecurityAttribute.cusip, "abcde",
                        SecurityAttribute.price, new BigDecimal("99.1234")));
        Security s2 = securityProvider.newSecurity("s2", Map.of(SecurityAttribute.cusip, "abcde",
                SecurityAttribute.currency, "USD", SecurityAttribute.duration, 3d));
        Security s3 = securityProvider.newSecurity("s3", Map.of(SecurityAttribute.description,
                "a security", SecurityAttribute.price, new BigDecimal("99.1000")));
        Security s4 = securityProvider.newSecurity("s4", Collections.emptyMap());
        // these are the same as above, with the attributes permuted; these should hash to the same
        Security s1a = securityProvider.newSecurity("s1",
                Map.of(SecurityAttribute.cusip, "abcde", SecurityAttribute.price,
                        new BigDecimal("99.1234"), SecurityAttribute.country, "US"));
        Security s2a = securityProvider.newSecurity("s2", Map.of(SecurityAttribute.cusip, "abcde",
                SecurityAttribute.duration, 3d, SecurityAttribute.currency, "USD"));
        Security s3a = securityProvider.newSecurity("s3", Map.of(SecurityAttribute.price,
                new BigDecimal("99.1000"), SecurityAttribute.description, "a security"));
        Security s4a = securityProvider.newSecurity("s4", Collections.emptyMap());

        HashSet<Security> securities = new HashSet<>();
        // adding the four distinct Securities any number of times should still result in four
        // distinct Securities
        securities.add(s1);
        securities.add(s2);
        securities.add(s3);
        securities.add(s4);
        securities.add(s1a);
        securities.add(s2a);
        securities.add(s3a);
        securities.add(s4a);
        securities.add(s1);
        securities.add(s2);
        securities.add(s4);
        securities.add(s2);
        securities.add(s4);
        securities.add(s1);
        securities.add(s3);

        assertEquals(4, securities.size(), "unexpected number of Securities");
        assertTrue(securities.contains(s1), "Securities should have contained s1");
        assertTrue(securities.contains(s2), "Securities should have contained s2");
        assertTrue(securities.contains(s3), "Securities should have contained s3");
        assertTrue(securities.contains(s4), "Securities should have contained s4");
    }
}
