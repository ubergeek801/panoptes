package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

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
                c -> "Emerging Markets".equals(
                        c.getPosition().getSecurity().getAttributeValue(SecurityAttribute.region)),
                null, 0.1, null);

        Security emergingMarketSecurity =
                securityProvider.newSecurity("em", Map.of(SecurityAttribute.region,
                        "Emerging Markets", SecurityAttribute.price, new BigDecimal("1.00")));
        Security usSecurity = securityProvider.newSecurity("us", Map.of(SecurityAttribute.region,
                "US", SecurityAttribute.price, new BigDecimal("1.00")));

        // create a Portfolio with 50% concentration in Emerging Markets
        HashSet<Position> positions = new HashSet<>();
        positions.add(new Position(100, emergingMarketSecurity));
        positions.add(new Position(100, usSecurity));
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertFalse(rule.evaluate(portfolio, null, new EvaluationContext()).isPassed(),
                "portfolio with > 10% concentration should have failed");

        // create a Portfolio with 10% concentration
        positions = new HashSet<>();
        positions.add(new Position(100, emergingMarketSecurity));
        positions.add(new Position(900, usSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(rule.evaluate(portfolio, null, new EvaluationContext()).isPassed(),
                "portfolio with == 10% concentration should have passed");

        // create a Portfolio with 5% concentration
        positions = new HashSet<>();
        positions.add(new Position(50, emergingMarketSecurity));
        positions.add(new Position(950, usSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(rule.evaluate(portfolio, null, new EvaluationContext()).isPassed(),
                "portfolio with == 5% concentration should have passed");

        // repeat the first test with a GroovyPositionFilter

        rule = new ConcentrationRule(null, "test rule",
                new GroovyPositionFilter("s.region == \"Emerging Markets\""), null, 0.1, null);

        // create a Portfolio with 50% concentration in Emerging Markets
        positions = new HashSet<>();
        positions.add(new Position(100, emergingMarketSecurity));
        positions.add(new Position(100, usSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        RuleResult result = rule.evaluate(portfolio, null, new EvaluationContext());
        assertFalse(result.isPassed(), "portfolio with > 10% concentration should have failed");
        assertNull(result.getException(), "rule should not have failed with exception");

        // repeat the first test with a faulty GroovyPositionFilter

        rule = new ConcentrationRule(null, "test rule",
                new GroovyPositionFilter("s.bogusProperty == \"Emerging Markets\""), null, 0.1,
                null);

        // create a Portfolio with 50% concentration in Emerging Markets
        positions = new HashSet<>();
        positions.add(new Position(100, emergingMarketSecurity));
        positions.add(new Position(100, usSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        result = rule.evaluate(portfolio, null, new EvaluationContext());
        assertFalse(result.isPassed(), "portfolio with > 10% concentration should have failed");
        assertNotNull(result.getException(), "rule should have failed with exception");
    }

    /**
     * Tests that benchmark-relative {@code ConcentrationRule} evaluation behaves as expected.
     */
    @Test
    public void testEvaluateBenchmarkRelative() {
        // the Rule tests that the concentration of currency = BRL is between 95% and 105% of the
        // benchmark
        ConcentrationRule rule = new ConcentrationRule(null, "test rule", c -> "BRL".equals(
                c.getPosition().getSecurity().getAttributeValue(SecurityAttribute.currency)), 0.95,
                1.05, null);

        Security brlSecurity =
                securityProvider.newSecurity("brl", Map.of(SecurityAttribute.currency, "BRL",
                        SecurityAttribute.price, new BigDecimal("1.00")));
        Security nzdSecurity =
                securityProvider.newSecurity("nzd", Map.of(SecurityAttribute.currency, "NZD",
                        SecurityAttribute.price, new BigDecimal("1.00")));

        // create a benchmark with 50% concentration in BRL
        HashSet<Position> benchmarkPositions = new HashSet<>();
        benchmarkPositions.add(new Position(100, brlSecurity));
        benchmarkPositions.add(new Position(100, nzdSecurity));
        final Portfolio benchmark1 =
                new Portfolio(new PortfolioKey("testBenchmark", 1), "test", benchmarkPositions);

        // create a Portfolio with 56% concentration in BRL
        HashSet<Position> positions = new HashSet<>();
        positions.add(new Position(56, brlSecurity));
        positions.add(new Position(44, nzdSecurity));
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertFalse(rule.evaluate(portfolio, benchmark1, new EvaluationContext()).isPassed(),
                "portfolio with == 56% concentration should have failed");

        // create a Portfolio with 44% concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(44, brlSecurity));
        positions.add(new Position(56, nzdSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertFalse(rule.evaluate(portfolio, benchmark1, new EvaluationContext()).isPassed(),
                "portfolio with == 44% concentration should have failed");

        // create a Portfolio with 52.5% (50% * 105%) concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(52.5, brlSecurity));
        positions.add(new Position(47.5, nzdSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(rule.evaluate(portfolio, benchmark1, new EvaluationContext()).isPassed(),
                "portfolio with == 52.5% concentration should have passed");

        // create a Portfolio with 47.5% (50% * 95%) concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(47.5, brlSecurity));
        positions.add(new Position(52.5, nzdSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(rule.evaluate(portfolio, benchmark1, new EvaluationContext()).isPassed(),
                "portfolio with == 52.5% concentration should have passed");

        // create a benchmark with 0% concentration in BRL
        benchmarkPositions = new HashSet<>();
        benchmarkPositions.add(new Position(100, nzdSecurity));
        final Portfolio benchmark2 =
                new Portfolio(new PortfolioKey("testBenchmark", 1), "test", benchmarkPositions);

        // any concentration is infinitely higher than the benchmark, so should fail
        assertFalse(rule.evaluate(portfolio, benchmark2, new EvaluationContext()).isPassed(),
                "portfolio with any concentration should have failed");

        // the Rule tests that the concentration of currency = BRL is at least 95% of the
        // benchmark
        rule = new ConcentrationRule(null, "test rule", c -> "BRL".equals(
                c.getPosition().getSecurity().getAttributeValue(SecurityAttribute.currency)), 0.95,
                null, null);

        // create a portfolio with 0% concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(100, nzdSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        // zero concentration is at least zero, so should pass
        assertTrue(rule.evaluate(portfolio, benchmark2, new EvaluationContext()).isPassed(),
                "portfolio with any concentration should have passed");

        // create a Portfolio with 1% concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(1, brlSecurity));
        positions.add(new Position(99, nzdSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        // any concentration is at least zero, so should pass
        assertTrue(rule.evaluate(portfolio, benchmark2, new EvaluationContext()).isPassed(),
                "portfolio with any concentration should have passed");

        // the Rule tests that the concentration of currency = BRL is at most 105% of the
        // benchmark
        rule = new ConcentrationRule(null, "test rule", c -> "BRL".equals(
                c.getPosition().getSecurity().getAttributeValue(SecurityAttribute.currency)), null,
                1.05, null);

        // any concentration is infinitely higher than the benchmark, so should fail
        assertFalse(rule.evaluate(portfolio, benchmark2, new EvaluationContext()).isPassed(),
                "portfolio with any concentration should have failed");
    }
}
