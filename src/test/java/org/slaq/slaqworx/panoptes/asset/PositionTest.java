package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;

/**
 * {@code PositionTest} tests the functionality of {@code Position}.
 *
 * @author jeremy
 */
public class PositionTest {
    /**
     * Tests that {@code Position}s are hashed in a reasonable way.
     */
    @Test
    public void testHash() {
        TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

        Security dummySecurity = securityProvider.newSecurity("dummy", Collections.emptyMap());
        Position p1 = new Position(new PositionKey("p1"), 100, dummySecurity);
        Position p2 = new Position(new PositionKey("p2"), 100, dummySecurity);
        Position p3 = new Position(new PositionKey("p3"), 100, dummySecurity);
        Position p4 = new Position(new PositionKey("p4"), 100, dummySecurity);
        Position p1a = new Position(new PositionKey("p1"), 100, dummySecurity);
        Position p2a = new Position(new PositionKey("p2"), 100, dummySecurity);
        Position p3a = new Position(new PositionKey("p3"), 100, dummySecurity);
        Position p4a = new Position(new PositionKey("p4"), 100, dummySecurity);

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

        assertEquals(4, positions.size(), "unexpected number of Positions");
        assertTrue(positions.contains(p1), "Positions should have contained p1");
        assertTrue(positions.contains(p2), "Positions should have contained p2");
        assertTrue(positions.contains(p3), "Positions should have contained p3");
        assertTrue(positions.contains(p4), "Positions should have contained p4");
    }
}
