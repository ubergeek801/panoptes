package org.slaq.slaqworx.panoptes.asset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;

/**
 * PositionTest tests the functionality of Position.
 *
 * @author jeremy
 */
public class PositionTest {
    /**
     * Tests that Positions are hashed in a reasonable way.
     */
    @Test
    public void testHash() {
        TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

        Security dummySecurity = securityProvider.newSecurity(Collections.emptyMap());
        Position p1 = new Position(new PositionKey("p1", 1), 100, dummySecurity.getKey());
        Position p2 = new Position(new PositionKey("p2", 1), 100, dummySecurity.getKey());
        Position p3 = new Position(new PositionKey("p3", 1), 100, dummySecurity.getKey());
        Position p4 = new Position(new PositionKey("p4", 1), 100, dummySecurity.getKey());
        Position p1a = new Position(new PositionKey("p1", 1), 100, dummySecurity.getKey());
        Position p2a = new Position(new PositionKey("p2", 1), 100, dummySecurity.getKey());
        Position p3a = new Position(new PositionKey("p3", 1), 100, dummySecurity.getKey());
        Position p4a = new Position(new PositionKey("p4", 1), 100, dummySecurity.getKey());

        HashSet<Position> positions = new HashSet<>();
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

        assertEquals("unexpected number of Positions", 4, positions.size());
        assertTrue("Positions should have contained p1", positions.contains(p1));
        assertTrue("Positions should have contained p2", positions.contains(p2));
        assertTrue("Positions should have contained p3", positions.contains(p3));
        assertTrue("Positions should have contained p4", positions.contains(p4));
    }
}
