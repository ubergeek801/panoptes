package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;

/**
 * MaterializedPositionTest tests the functionality of MaterializedPosition.
 *
 * @author jeremy
 */
public class MaterializedPositionTest {
    /**
     * Tests that Positions are hashed in a reasonable way.
     */
    @Test
    public void testHash() {
        TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

        Security dummySecurity = securityProvider.newSecurity(Collections.emptyMap());
        MaterializedPosition p1 =
                new MaterializedPosition(new PositionKey("p1"), 100, dummySecurity.getKey());
        MaterializedPosition p2 =
                new MaterializedPosition(new PositionKey("p2"), 100, dummySecurity.getKey());
        MaterializedPosition p3 =
                new MaterializedPosition(new PositionKey("p3"), 100, dummySecurity.getKey());
        MaterializedPosition p4 =
                new MaterializedPosition(new PositionKey("p4"), 100, dummySecurity.getKey());
        MaterializedPosition p1a =
                new MaterializedPosition(new PositionKey("p1"), 100, dummySecurity.getKey());
        MaterializedPosition p2a =
                new MaterializedPosition(new PositionKey("p2"), 100, dummySecurity.getKey());
        MaterializedPosition p3a =
                new MaterializedPosition(new PositionKey("p3"), 100, dummySecurity.getKey());
        MaterializedPosition p4a =
                new MaterializedPosition(new PositionKey("p4"), 100, dummySecurity.getKey());

        HashSet<MaterializedPosition> positions = new HashSet<>();
        // adding the four distinct Positions any number of times should still result in four
        // distinct Positions
        positions.add(p1);
        positions.add(p2);
        positions.add(p3);
        positions.add(p4);
        positions.add(p1a);
        positions.add(p2a);
        positions.add(p3a);
        positions.add(p4a);
        positions.add(p1);
        positions.add(p2);
        positions.add(p4);
        positions.add(p2);
        positions.add(p4);
        positions.add(p1);
        positions.add(p3);

        assertEquals(4, positions.size(), "unexpected number of Positions");
        assertTrue(positions.contains(p1), "Positions should have contained p1");
        assertTrue(positions.contains(p2), "Positions should have contained p2");
        assertTrue(positions.contains(p3), "Positions should have contained p3");
        assertTrue(positions.contains(p4), "Positions should have contained p4");
    }
}
