package org.slaq.slaqworx.panoptes.asset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;

/**
 * SecurityTest tests the functionality of Security.
 *
 * @author jeremy
 */
public class SecurityTest {
    /**
     * Tests that Securities are hashed in a reasonable way.
     */
    @Test
    public void testHash() {
        TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

        Security s1 = securityProvider
                .newSecurity(Map.of(TestUtil.country, "US", TestUtil.cusip, "abcde"));
        Security s2 = securityProvider.newSecurity(
                Map.of(TestUtil.cusip, "abcde", TestUtil.currency, "USD", TestUtil.duration, 3d));
        Security s3 = securityProvider.newSecurity(Map.of(TestUtil.description, "a security"));
        Security s4 = securityProvider.newSecurity(Collections.emptyMap());
        // these are the same as above, with the attributes permuted; these should hash to the same
        Security s1a = securityProvider
                .newSecurity(Map.of(TestUtil.cusip, "abcde", TestUtil.country, "US"));
        Security s2a = securityProvider.newSecurity(
                Map.of(TestUtil.cusip, "abcde", TestUtil.duration, 3d, TestUtil.currency, "USD"));
        Security s3a = securityProvider.newSecurity(Map.of(TestUtil.description, "a security"));
        Security s4a = securityProvider.newSecurity(Collections.emptyMap());

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

        assertEquals("unexpected number of Securities", 4, securities.size());
        assertTrue("Securities should have contained s1", securities.contains(s1));
        assertTrue("Securities should have contained s2", securities.contains(s2));
        assertTrue("Securities should have contained s3", securities.contains(s3));
        assertTrue("Securities should have contained s4", securities.contains(s4));
    }
}
