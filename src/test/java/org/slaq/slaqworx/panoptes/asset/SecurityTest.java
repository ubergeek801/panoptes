package org.slaq.slaqworx.panoptes.asset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

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
        Security s1 = new Security("s1", Collections.emptyMap());
        Security s2 = new Security("s2", Collections.emptyMap());
        Security s3 = new Security("s3", Collections.emptyMap());
        Security s4 = new Security("s4", Collections.emptyMap());
        Security s1a = new Security("s1", Collections.emptyMap());
        Security s2a = new Security("s2", Collections.emptyMap());
        Security s3a = new Security("s3", Collections.emptyMap());
        Security s4a = new Security("s4", Collections.emptyMap());

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
