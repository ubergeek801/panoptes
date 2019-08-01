package org.slaq.slaqworx.panoptes.asset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;

/**
 * PortfolioTest tests the functionality of Portfolio.
 *
 * @author jeremy
 */
public class PortfolioTest {
    private TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

    /**
     * Tests that getPositions() behaves as expected.
     */
    @Test
    public void getPositions() {
        Security dummySecurity = securityProvider.newSecurity(Collections.emptyMap());
        HashSet<Position> positions = new HashSet<>();
        positions.add(new Position(100, dummySecurity.getKey()));
        positions.add(new Position(200, dummySecurity.getKey()));
        positions.add(new Position(300, dummySecurity.getKey()));
        positions.add(new Position(400, dummySecurity.getKey()));

        Portfolio portfolio = new Portfolio(new PortfolioKey("p1", 1), "test", positions);

        Stream<Position> stream1 = portfolio.getPositions();
        Stream<Position> stream2 = portfolio.getPositions();

        assertFalse("position streams should be distinct", stream1 == stream2);
        assertFalse("position streams should be distinct", stream1.equals(stream2));
        assertEquals("unexpected count for stream 1", 4, stream1.count());
        assertEquals("unexpected count for stream 2", 4, stream2.count());

        // ensure that both streams can be iterated independently/simultaneously
        Iterator<Position> iter1 = portfolio.getPositions().iterator();
        Iterator<Position> iter2 = portfolio.getPositions().iterator();
        int itemCount = 0;
        while (iter1.hasNext()) {
            iter1.next();
            iter2.next();
            itemCount++;
        }
        assertFalse("stream 2 should be exhausted", iter2.hasNext());
        assertEquals("unexpected count for parallel stream iteration", 4, itemCount);
    }

    /**
     * Tests that getTotalAmount() behaves as expected.
     */
    @Test
    public void testGetTotalAmount() {
        Security dummySecurity = securityProvider.newSecurity(Collections.emptyMap());
        HashSet<Position> positions = new HashSet<>();
        positions.add(new Position(100, dummySecurity.getKey()));
        positions.add(new Position(200, dummySecurity.getKey()));
        positions.add(new Position(300, dummySecurity.getKey()));
        positions.add(new Position(400, dummySecurity.getKey()));

        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);
        // the total amount is merely the sum of the amounts 100 + 200 + 300 + 400 = 1000
        assertEquals("unexpected total amount", 1000, portfolio.getTotalAmount(), TestUtil.EPSILON);
    }

    /**
     * Tests that Portfolios are hashed in a reasonable way.
     */
    @Test
    public void testHash() {
        Portfolio p1 = new Portfolio(new PortfolioKey("p1", 1), "test", Collections.emptySet());
        Portfolio p2 = new Portfolio(new PortfolioKey("p2", 1), "test", Collections.emptySet());
        Portfolio p3 = new Portfolio(new PortfolioKey("p3", 1), "test", Collections.emptySet());
        Portfolio p4 = new Portfolio(new PortfolioKey("p4", 1), "test", Collections.emptySet());
        Portfolio p1a = new Portfolio(new PortfolioKey("p1", 1), "test", Collections.emptySet());
        Portfolio p2a = new Portfolio(new PortfolioKey("p2", 1), "test", Collections.emptySet());
        Portfolio p3a = new Portfolio(new PortfolioKey("p3", 1), "test", Collections.emptySet());
        Portfolio p4a = new Portfolio(new PortfolioKey("p4", 1), "test", Collections.emptySet());

        HashSet<Portfolio> portfolios = new HashSet<>();
        // adding the four distinct Portfolios any number of times should still result in four
        // distinct Portfolios
        portfolios.add(p1);
        portfolios.add(p2);
        portfolios.add(p3);
        portfolios.add(p4);
        portfolios.add(p1a);
        portfolios.add(p2a);
        portfolios.add(p3a);
        portfolios.add(p4a);
        portfolios.add(p1);
        portfolios.add(p2);
        portfolios.add(p4);
        portfolios.add(p2);
        portfolios.add(p4);
        portfolios.add(p1);
        portfolios.add(p3);

        assertEquals("unexpected number of Portfolios", 4, portfolios.size());
        assertTrue("Portfolios should have contained p1", portfolios.contains(p1));
        assertTrue("Portfolios should have contained p2", portfolios.contains(p2));
        assertTrue("Portfolios should have contained p3", portfolios.contains(p3));
        assertTrue("Portfolios should have contained p4", portfolios.contains(p4));
    }
}
