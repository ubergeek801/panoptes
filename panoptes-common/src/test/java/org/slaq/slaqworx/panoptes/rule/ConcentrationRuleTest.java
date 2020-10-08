package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;
import org.slaq.slaqworx.panoptes.test.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * {@code ConcentrationRuleTest} tests the functionality of the {@code ConcentrationRule}.
 *
 * @author jeremy
 */
public class ConcentrationRuleTest {
    private TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

    /**
     * Tests that absolute {@code ConcentrationRule} evaluation behaves as expected.
     */
    @Test
    public void testEvaluate() {
        // the Rule tests that the concentration of region = "Emerging Markets" <= 10%
        ConcentrationRule rule = new ConcentrationRule(null, "test rule",
                c -> "Emerging Markets".equals(c.getPosition().getAttributeValue(
                        SecurityAttribute.region, TestUtil.defaultTestEvaluationContext())),
                null, 0.1, null);

        Security emergingMarketSecurity = securityProvider.newSecurity("em", SecurityAttribute
                .mapOf(SecurityAttribute.region, "Emerging Markets", SecurityAttribute.price, 1d));
        Security usSecurity = securityProvider.newSecurity("us", SecurityAttribute
                .mapOf(SecurityAttribute.region, "US", SecurityAttribute.price, 1d));

        // create a Portfolio with 50% concentration in Emerging Markets
        HashSet<Position> positions = new HashSet<>();
        positions.add(new SimplePosition(100, emergingMarketSecurity.getKey()));
        positions.add(new SimplePosition(100, usSecurity.getKey()));
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertFalse(rule.evaluate(portfolio, null, null, TestUtil.defaultTestEvaluationContext())
                .isPassed(), "portfolio with > 10% concentration should have failed");

        // create a Portfolio with 10% concentration
        positions = new HashSet<>();
        positions.add(new SimplePosition(100, emergingMarketSecurity.getKey()));
        positions.add(new SimplePosition(900, usSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(rule.evaluate(portfolio, null, null, TestUtil.defaultTestEvaluationContext())
                .isPassed(), "portfolio with == 10% concentration should have passed");

        // create a Portfolio with 5% concentration
        positions = new HashSet<>();
        positions.add(new SimplePosition(50, emergingMarketSecurity.getKey()));
        positions.add(new SimplePosition(950, usSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(rule.evaluate(portfolio, null, null, TestUtil.defaultTestEvaluationContext())
                .isPassed(), "portfolio with == 5% concentration should have passed");

        // repeat the first test with a GroovyPositionFilter

        rule = new ConcentrationRule(null, "test rule",
                GroovyPositionFilter.of("s.region == 'Emerging Markets'"), null, 0.1, null);

        // create a Portfolio with 50% concentration in Emerging Markets
        positions = new HashSet<>();
        positions.add(new SimplePosition(100, emergingMarketSecurity.getKey()));
        positions.add(new SimplePosition(100, usSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        ValueResult result =
                rule.evaluate(portfolio, null, null, TestUtil.defaultTestEvaluationContext());
        assertFalse(result.isPassed(), "portfolio with > 10% concentration should have failed");
        assertNull(result.getException(), "rule should not have failed with exception");

        // repeat the first test with a faulty GroovyPositionFilter

        rule = new ConcentrationRule(null, "test rule",
                GroovyPositionFilter.of("s.bogusProperty == 'Emerging Markets'"), null, 0.1, null);

        // create a Portfolio with 50% concentration in Emerging Markets
        positions = new HashSet<>();
        positions.add(new SimplePosition(100, emergingMarketSecurity.getKey()));
        positions.add(new SimplePosition(100, usSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        result = rule.evaluate(portfolio, null, null, TestUtil.defaultTestEvaluationContext());
        assertFalse(result.isPassed(), "portfolio with > 10% concentration should have failed");
        assertNotNull(result.getException(), "rule should have failed with exception");
    }

    /**
     * Tests that benchmark-relative {@code ConcentrationRule} evaluation behaves as expected.
     */
    @Test
    public void testEvaluate_benchmarkRelative() {
        // the Rule tests that the concentration of currency = BRL is between 95% and 105% of the
        // benchmark
        ConcentrationRule rule = new ConcentrationRule(null, "test rule",
                c -> "BRL".equals(c.getPosition().getAttributeValue(SecurityAttribute.currency,
                        TestUtil.defaultTestEvaluationContext())),
                0.95, 1.05, null);

        Security brlSecurity = securityProvider.newSecurity("brl", SecurityAttribute
                .mapOf(SecurityAttribute.currency, "BRL", SecurityAttribute.price, 1d));
        Security nzdSecurity = securityProvider.newSecurity("nzd", SecurityAttribute
                .mapOf(SecurityAttribute.currency, "NZD", SecurityAttribute.price, 1d));

        // create a benchmark with 50% concentration in BRL
        HashSet<Position> benchmarkPositions = new HashSet<>();
        benchmarkPositions.add(new SimplePosition(100, brlSecurity.getKey()));
        benchmarkPositions.add(new SimplePosition(100, nzdSecurity.getKey()));
        final Portfolio benchmark1 =
                new Portfolio(new PortfolioKey("testBenchmark", 1), "test", benchmarkPositions);

        // create a Portfolio with 56% concentration in BRL
        HashSet<Position> positions = new HashSet<>();
        positions.add(new SimplePosition(56, brlSecurity.getKey()));
        positions.add(new SimplePosition(44, nzdSecurity.getKey()));
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertFalse(
                rule.evaluate(portfolio, benchmark1, null, TestUtil.defaultTestEvaluationContext())
                        .isPassed(),
                "portfolio with == 56% concentration should have failed");

        // create a Portfolio with 44% concentration in BRL
        positions = new HashSet<>();
        positions.add(new SimplePosition(44, brlSecurity.getKey()));
        positions.add(new SimplePosition(56, nzdSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertFalse(
                rule.evaluate(portfolio, benchmark1, null, TestUtil.defaultTestEvaluationContext())
                        .isPassed(),
                "portfolio with == 44% concentration should have failed");

        // create a Portfolio with 52.5% (50% * 105%) concentration in BRL
        positions = new HashSet<>();
        positions.add(new SimplePosition(52.5, brlSecurity.getKey()));
        positions.add(new SimplePosition(47.5, nzdSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(
                rule.evaluate(portfolio, benchmark1, null, TestUtil.defaultTestEvaluationContext())
                        .isPassed(),
                "portfolio with == 52.5% concentration should have passed");

        // create a Portfolio with 47.5% (50% * 95%) concentration in BRL
        positions = new HashSet<>();
        positions.add(new SimplePosition(47.5, brlSecurity.getKey()));
        positions.add(new SimplePosition(52.5, nzdSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(
                rule.evaluate(portfolio, benchmark1, null, TestUtil.defaultTestEvaluationContext())
                        .isPassed(),
                "portfolio with == 52.5% concentration should have passed");

        // create a benchmark with 0% concentration in BRL
        benchmarkPositions = new HashSet<>();
        benchmarkPositions.add(new SimplePosition(100, nzdSecurity.getKey()));
        final Portfolio benchmark2 =
                new Portfolio(new PortfolioKey("testBenchmark", 1), "test", benchmarkPositions);

        // any concentration is infinitely higher than the benchmark, so should fail
        assertFalse(
                rule.evaluate(portfolio, benchmark2, null, TestUtil.defaultTestEvaluationContext())
                        .isPassed(),
                "portfolio with any concentration should have failed");

        // the Rule tests that the concentration of currency = BRL is at least 95% of the
        // benchmark
        rule = new ConcentrationRule(null, "test rule",
                c -> "BRL".equals(c.getPosition().getAttributeValue(SecurityAttribute.currency,
                        TestUtil.defaultTestEvaluationContext())),
                0.95, null, null);

        // create a portfolio with 0% concentration in BRL
        positions = new HashSet<>();
        positions.add(new SimplePosition(100, nzdSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        // zero concentration is at least zero, so should pass
        assertTrue(
                rule.evaluate(portfolio, benchmark2, null, TestUtil.defaultTestEvaluationContext())
                        .isPassed(),
                "portfolio with any concentration should have passed");

        // create a Portfolio with 1% concentration in BRL
        positions = new HashSet<>();
        positions.add(new SimplePosition(1, brlSecurity.getKey()));
        positions.add(new SimplePosition(99, nzdSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        // any concentration is at least zero, so should pass
        assertTrue(
                rule.evaluate(portfolio, benchmark2, null, TestUtil.defaultTestEvaluationContext())
                        .isPassed(),
                "portfolio with any concentration should have passed");

        // the Rule tests that the concentration of currency = BRL is at most 105% of the
        // benchmark
        rule = new ConcentrationRule(null, "test rule",
                c -> "BRL".equals(c.getPosition().getAttributeValue(SecurityAttribute.currency,
                        TestUtil.defaultTestEvaluationContext())),
                null, 1.05, null);

        // any concentration is infinitely higher than the benchmark, so should fail
        assertFalse(
                rule.evaluate(portfolio, benchmark2, null, TestUtil.defaultTestEvaluationContext())
                        .isPassed(),
                "portfolio with any concentration should have failed");
    }
}
