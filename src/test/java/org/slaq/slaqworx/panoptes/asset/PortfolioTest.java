package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * {@code PortfolioTest} tests the functionality of {@code Portfolio}.
 *
 * @author jeremy
 */
public class PortfolioTest {
    private TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

    /**
     * Tests that {@code getPositions()} behaves as expected.
     */
    @Test
    public void testGetPositions() {
        Security dummySecurity = securityProvider.newSecurity("dummy",
                Map.of(SecurityAttribute.price, new BigDecimal("1.00")));
        HashSet<Position> positions = new HashSet<>();
        positions.add(new Position(100, dummySecurity.getKey()));
        positions.add(new Position(200, dummySecurity.getKey()));
        positions.add(new Position(300, dummySecurity.getKey()));
        positions.add(new Position(400, dummySecurity.getKey()));

        Portfolio portfolio = new Portfolio(new PortfolioKey("p1", 1), "test", positions);

        try (Stream<Position> stream1 = portfolio.getPositions();
                Stream<Position> stream2 = portfolio.getPositions()) {
            assertFalse(stream1 == stream2, "position streams should be distinct");
            assertFalse(stream1.equals(stream2), "position streams should be distinct");
            assertEquals(4, stream1.count(), "unexpected count for stream 1");
            assertEquals(4, stream2.count(), "unexpected count for stream 2");

            // ensure that both streams can be iterated independently/simultaneously
            Iterator<Position> iter1 = portfolio.getPositions().iterator();
            Iterator<Position> iter2 = portfolio.getPositions().iterator();
            int itemCount = 0;
            while (iter1.hasNext()) {
                iter1.next();
                iter2.next();
                itemCount++;
            }
            assertFalse(iter2.hasNext(), "stream 2 should be exhausted");
            assertEquals(4, itemCount, "unexpected count for parallel stream iteration");
        }
    }

    /**
     * Tests that {@code getTotalMarketValue()} behaves as expected.
     */
    @Test
    public void testGetTotalMarketValue() {
        EvaluationContext evaluationContext = TestUtil.defaultTestEvaluationContext;

        Security dummySecurity = securityProvider.newSecurity("dummy",
                Map.of(SecurityAttribute.price, new BigDecimal("2.00")));
        HashSet<Position> positions = new HashSet<>();
        positions.add(new Position(100, dummySecurity.getKey()));
        positions.add(new Position(200, dummySecurity.getKey()));
        positions.add(new Position(300, dummySecurity.getKey()));
        positions.add(new Position(400, dummySecurity.getKey()));

        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);
        // the total amount is merely the sum of the amounts (100 + 200 + 300 + 400) * 2.00 = 2000
        assertEquals(2000, portfolio.getTotalMarketValue(evaluationContext), TestUtil.EPSILON,
                "unexpected total amount");
    }

    /**
     * Tests that {@code Portfolios} are hashed in a reasonable way.
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

        assertEquals(4, portfolios.size(), "unexpected number of Portfolios");
        assertTrue(portfolios.contains(p1), "Portfolios should have contained p1");
        assertTrue(portfolios.contains(p2), "Portfolios should have contained p2");
        assertTrue(portfolios.contains(p3), "Portfolios should have contained p3");
        assertTrue(portfolios.contains(p4), "Portfolios should have contained p4");
    }
}
