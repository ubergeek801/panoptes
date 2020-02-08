package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestPortfolioProvider;
import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.HierarchicalPositionSupplier.PositionHierarchyOption;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * {@code PortfolioTest} tests the functionality of the {@code Portfolio}.
 *
 * @author jeremy
 */
public class PortfolioTest {
    private TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();
    private TestPortfolioProvider portfolioProvider = TestUtil.testPortfolioProvider();

    /**
     * Tests that {@code getPositions()} behaves as expected for non-hierarchy requests.
     */
    @Test
    public void testGetPositions() {
        Security dummySecurity = securityProvider.newSecurity("dummy",
                Map.of(SecurityAttribute.price, new BigDecimal("1.00")));
        HashSet<Position> positions = new HashSet<>();
        positions.add(new SimplePosition(100, dummySecurity.getKey()));
        positions.add(new SimplePosition(200, dummySecurity.getKey()));
        positions.add(new SimplePosition(300, dummySecurity.getKey()));
        positions.add(new SimplePosition(400, dummySecurity.getKey()));

        Portfolio portfolio = new Portfolio(new PortfolioKey("p1", 1), "test", positions);

        try (Stream<? extends Position> stream1 = portfolio.getPositions();
                Stream<? extends Position> stream2 = portfolio.getPositions()) {
            assertFalse(stream1 == stream2, "position streams should be distinct");
            assertFalse(stream1.equals(stream2), "position streams should be distinct");
            assertEquals(4, stream1.count(), "unexpected count for stream 1");
            assertEquals(4, stream2.count(), "unexpected count for stream 2");

            // ensure that both streams can be iterated independently/simultaneously
            Iterator<? extends Position> iter1 = portfolio.getPositions().iterator();
            Iterator<? extends Position> iter2 = portfolio.getPositions().iterator();
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
     * Tests that {@code getPositions()} works as expected when lookthrough {@code Position}s are
     * requested.
     */
    @Test
    public void testGetPositions_lookthrough() {
        // just a plain old Security (no lookthrough)
        Security plainSecurity1 = securityProvider.newSecurity("sec1",
                Map.of(SecurityAttribute.price, new BigDecimal("1.00")));

        // these Securities have a Portfolio key associated and thus represent holdings in those
        // funds
        PortfolioKey p2Key = new PortfolioKey("p2", 1);
        Security p2Security = securityProvider.newSecurity("p2Sec",
                Map.of(SecurityAttribute.price, new BigDecimal("1.00"), SecurityAttribute.portfolio,
                        p2Key, SecurityAttribute.amount, 1000d));
        PortfolioKey p3Key = new PortfolioKey("p3", 1);
        Security p3Security = securityProvider.newSecurity("p3Sec",
                Map.of(SecurityAttribute.price, new BigDecimal("1.00"), SecurityAttribute.portfolio,
                        p3Key, SecurityAttribute.amount, 1000d));
        PortfolioKey p4Key = new PortfolioKey("p4", 1);
        Security p4Security = securityProvider.newSecurity("p4Sec",
                Map.of(SecurityAttribute.price, new BigDecimal("1.00"), SecurityAttribute.portfolio,
                        p4Key, SecurityAttribute.amount, 1000d));

        // p1 is 20% p2 and 20% p3
        HashSet<Position> p1Positions = new HashSet<>();
        p1Positions.add(new SimplePosition(300, plainSecurity1.getKey()));
        p1Positions.add(new SimplePosition(100, p2Security.getKey()));
        p1Positions.add(new SimplePosition(100, p3Security.getKey()));
        Portfolio p1 = portfolioProvider.newPortfolio("p1", "test1", p1Positions, null,
                Collections.emptySet());

        // p2 is 20% p3 and 20% p4, with amount 1000
        HashSet<Position> p2Positions = new HashSet<>();
        p2Positions.add(new SimplePosition(600, plainSecurity1.getKey()));
        p2Positions.add(new SimplePosition(200, p3Security.getKey()));
        p2Positions.add(new SimplePosition(200, p4Security.getKey()));
        portfolioProvider.newPortfolio(p2Key, "test2", p2Positions, null, Collections.emptySet());

        // p3 is 50% p4, with amount 1000
        HashSet<Position> p3Positions = new HashSet<>();
        p3Positions.add(new SimplePosition(100, plainSecurity1.getKey()));
        p3Positions.add(new SimplePosition(100, p4Security.getKey()));
        portfolioProvider.newPortfolio(p3Key, "test3", p3Positions, null, Collections.emptySet());

        // p4 holds no other Portfolios and has amount 1000
        HashSet<Position> p4Positions = new HashSet<>();
        p4Positions.add(new SimplePosition(500, plainSecurity1.getKey()));
        portfolioProvider.newPortfolio(p4Key, "test4", p4Positions, null, Collections.emptySet());

        // lookthrough of p1 should see:
        // 300 sec1
        // + 100 p2
        // + 100 p3
        // which expands to
        // 300 sec1
        // + (100 / 1000) * 600 sec1 [p2]
        // + (100 / 1000) * 200 p3 [p2]
        // + (100 / 1000) * 200 p4 [p2]
        // + (100 / 1000) * 100 sec1 [p3]
        // + (100 / 1000) * 100 p4 [p3]
        // which again expands to
        // 300 sec1
        // + (100 / 1000) * 600 sec1 [p2]
        // + (100 / 1000) * (200 / 1000) * 100 sec1 [p2][p3]
        // + (100 / 1000) * (200 / 1000) * 100 p4 [p2][p3]
        // + (100 / 1000) * (200 / 1000) * 500 sec1 [p2][p4]
        // + (100 / 1000) * 100 sec1 [p3]
        // + (100 / 1000) * (100 / 1000) * 500 sec1 [p3][p4]
        // and finally to
        // 300 sec1
        // + (100 / 1000) * 600 sec1 [p2]
        // + (100 / 1000) * (200 / 1000) * 100 sec1 [p2][p3]
        // + (100 / 1000) * (200 / 1000) * (100 / 1000) * 500 sec1 [p2][p3][p4]
        // + (100 / 1000) * (200 / 1000) * 500 sec1 [p2][p4]
        // + (100 / 1000) * 100 sec1 [p3]
        // + (100 / 1000) * (100 / 1000) * 500 sec1 [p3][p4]
        // which amounts to these Positions in sec1:
        // 300
        // 60
        // 2
        // 1
        // 10
        // 10
        // 5

        EvaluationContext evaluationContext = TestUtil.defaultTestEvaluationContext();
        List<? extends Position> lookthroughPositions =
                p1.getPositions(EnumSet.of(PositionHierarchyOption.LOOKTHROUGH), evaluationContext)
                        .collect(Collectors.toList());

        assertEquals(7, lookthroughPositions.size(),
                "unexpected total number of lookthrough Positions");
        // this is rather tedious
        boolean is300PositionFound = false;
        boolean is60PositionFound = false;
        boolean is2PositionFound = false;
        boolean is1PositionFound = false;
        boolean is10Position1Found = false;
        boolean is10Position2Found = false;
        boolean is5PositionFound = false;
        for (Position p : lookthroughPositions) {
            double amount = p.getAmount();
            if (amount == 300) {
                is300PositionFound = true;
            } else if (amount == 60) {
                is60PositionFound = true;
            } else if (amount == 2) {
                is2PositionFound = true;
            } else if (amount == 1) {
                is1PositionFound = true;
            } else if (amount == 10) {
                if (!is10Position1Found) {
                    is10Position1Found = true;
                } else {
                    is10Position2Found = true;
                }
            } else if (amount == 5) {
                is5PositionFound = true;
            } else {
                fail("found unexpected Position amount " + amount);
            }
        }
        assertTrue(is300PositionFound, "expected to find Position with amount 300");
        assertTrue(is60PositionFound, "expected to find Position with amount 60");
        assertTrue(is2PositionFound, "expected to find Position with amount 2");
        assertTrue(is1PositionFound, "expected to find Position with amount 1");
        assertTrue(is10Position1Found, "expected to find Position with amount 10");
        assertTrue(is10Position2Found, "expected to find second Position with amount 10");
        assertTrue(is5PositionFound, "expected to find Position with amount 5");
    }

    /**
     * Tests that {@code getPositions()} works as expected when {@code TaxLot}s are requested.
     */
    @Test
    public void testGetPositions_taxLot() {
        // FIXME implement testGetPositions_taxLot()
    }

    /**
     * Tests that {@code getTotalMarketValue()} behaves as expected.
     */
    @Test
    public void testGetTotalMarketValue() {
        Security dummySecurity = securityProvider.newSecurity("dummy",
                Map.of(SecurityAttribute.price, new BigDecimal("2.00")));
        HashSet<Position> positions = new HashSet<>();
        positions.add(new SimplePosition(100, dummySecurity.getKey()));
        positions.add(new SimplePosition(200, dummySecurity.getKey()));
        positions.add(new SimplePosition(300, dummySecurity.getKey()));
        positions.add(new SimplePosition(400, dummySecurity.getKey()));

        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);
        // the total amount is merely the sum of the amounts (100 + 200 + 300 + 400) * 2.00 = 2000
        assertEquals(2000, portfolio.getTotalMarketValue(TestUtil.defaultTestEvaluationContext()),
                TestUtil.EPSILON, "unexpected total amount");
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
