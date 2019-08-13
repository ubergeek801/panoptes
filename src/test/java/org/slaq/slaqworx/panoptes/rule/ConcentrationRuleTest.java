package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;

/**
 * ConcentrationRuleTest tests the functionality of the ConcentrationRule.
 *
 * @author jeremy
 */
public class ConcentrationRuleTest {
    private TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

    /**
     * Tests that absolute ConcentrationRule evaluation behaves as expected.
     */
    @Test
    public void testEvaluate() {
        // the rule tests that the concentration of region = "Emerging Markets" <= 10%
        ConcentrationRule rule = new ConcentrationRule(
                null, "test rule", c -> "Emerging Markets".equals(c.getPosition()
                        .getSecurity(securityProvider).getAttributeValue(TestUtil.region)),
                null, 0.1, null);

        Security emergingMarketSecurity =
                securityProvider.newSecurity(Map.of(TestUtil.region, "Emerging Markets"));
        Security usSecurity = securityProvider.newSecurity(Map.of(TestUtil.region, "US"));

        // create a portfolio with 50% concentration in Emerging Markets
        HashSet<Position> positions = new HashSet<>();
        positions.add(new MaterializedPosition(100, emergingMarketSecurity.getKey()));
        positions.add(new MaterializedPosition(100, usSecurity.getKey()));
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertFalse(
                rule.evaluate(portfolio, null, new EvaluationContext(null, securityProvider, null))
                        .isPassed(),
                "portfolio with > 10% concentration should have failed");

        // create a portfolio with 10% concentration
        positions = new HashSet<>();
        positions.add(new MaterializedPosition(100, emergingMarketSecurity.getKey()));
        positions.add(new MaterializedPosition(900, usSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(
                rule.evaluate(portfolio, null, new EvaluationContext(null, securityProvider, null))
                        .isPassed(),
                "portfolio with == 10% concentration should have passed");

        // create a portfolio with 5% concentration
        positions = new HashSet<>();
        positions.add(new MaterializedPosition(50, emergingMarketSecurity.getKey()));
        positions.add(new MaterializedPosition(950, usSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(
                rule.evaluate(portfolio, null, new EvaluationContext(null, securityProvider, null))
                        .isPassed(),
                "portfolio with == 5% concentration should have passed");

        // repeat the first test with a GroovyPositionFilter

        rule = new ConcentrationRule(null, "test rule",
                new GroovyPositionFilter("s.region == \"Emerging Markets\""), null, 0.1, null);

        // create a portfolio with 50% concentration in Emerging Markets
        positions = new HashSet<>();
        positions.add(new MaterializedPosition(100, emergingMarketSecurity.getKey()));
        positions.add(new MaterializedPosition(100, usSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        EvaluationResult result =
                rule.evaluate(portfolio, null, new EvaluationContext(null, securityProvider, null));
        assertFalse(result.isPassed(), "portfolio with > 10% concentration should have failed");
        assertNull(result.getException(), "rule should not have failed with exception");

        // repeat the first test with a faulty GroovyPositionFilter

        rule = new ConcentrationRule(null, "test rule",
                new GroovyPositionFilter("s.bogusProperty == \"Emerging Markets\""), null, 0.1,
                null);

        // create a portfolio with 50% concentration in Emerging Markets
        positions = new HashSet<>();
        positions.add(new MaterializedPosition(100, emergingMarketSecurity.getKey()));
        positions.add(new MaterializedPosition(100, usSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        result = rule.evaluate(portfolio, null,
                new EvaluationContext(null, securityProvider, null));
        assertFalse(result.isPassed(), "portfolio with > 10% concentration should have failed");
        assertNotNull(result.getException(), "rule should have failed with exception");
    }

    /**
     * Tests that benchmark-relative ConcentrationRule evaluation behaves as expected.
     */
    @Test
    public void testEvaluateBenchmarkRelative() {
        // the rule tests that the concentration of currency = BRL is between 95% and 105% of the
        // benchmark
        ConcentrationRule rule = new ConcentrationRule(null, "test rule", c -> "BRL".equals(
                c.getPosition().getSecurity(securityProvider).getAttributeValue(TestUtil.currency)),
                0.95, 1.05, null);

        Security brlSecurity = securityProvider.newSecurity(Map.of(TestUtil.currency, "BRL"));
        Security nzdSecurity = securityProvider.newSecurity(Map.of(TestUtil.currency, "NZD"));

        // create a benchmark with 50% concentration in BRL
        HashSet<Position> benchmarkPositions = new HashSet<>();
        benchmarkPositions.add(new MaterializedPosition(100, brlSecurity.getKey()));
        benchmarkPositions.add(new MaterializedPosition(100, nzdSecurity.getKey()));
        final Portfolio benchmark1 =
                new Portfolio(new PortfolioKey("testBenchmark", 1), "test", benchmarkPositions);
        // a really dumb PortfolioProvider that always returns benchmark1
        PortfolioProvider benchmark1Provider = (key -> benchmark1);

        // create a portfolio with 56% concentration in BRL
        HashSet<Position> positions = new HashSet<>();
        positions.add(new MaterializedPosition(56, brlSecurity.getKey()));
        positions.add(new MaterializedPosition(44, nzdSecurity.getKey()));
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertFalse(rule
                .evaluate(portfolio, benchmark1,
                        new EvaluationContext(benchmark1Provider, securityProvider, null))
                .isPassed(), "portfolio with == 56% concentration should have failed");

        // create a portfolio with 44% concentration in BRL
        positions = new HashSet<>();
        positions.add(new MaterializedPosition(44, brlSecurity.getKey()));
        positions.add(new MaterializedPosition(56, nzdSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertFalse(rule
                .evaluate(portfolio, benchmark1,
                        new EvaluationContext(benchmark1Provider, securityProvider, null))
                .isPassed(), "portfolio with == 44% concentration should have failed");

        // create a portfolio with 52.5% (50% * 105%) concentration in BRL
        positions = new HashSet<>();
        positions.add(new MaterializedPosition(52.5, brlSecurity.getKey()));
        positions.add(new MaterializedPosition(47.5, nzdSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(rule
                .evaluate(portfolio, benchmark1,
                        new EvaluationContext(benchmark1Provider, securityProvider, null))
                .isPassed(), "portfolio with == 52.5% concentration should have passed");

        // create a portfolio with 47.5% (50% * 95%) concentration in BRL
        positions = new HashSet<>();
        positions.add(new MaterializedPosition(47.5, brlSecurity.getKey()));
        positions.add(new MaterializedPosition(52.5, nzdSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(rule
                .evaluate(portfolio, benchmark1,
                        new EvaluationContext(benchmark1Provider, securityProvider, null))
                .isPassed(), "portfolio with == 52.5% concentration should have passed");

        // create a benchmark with 0% concentration in BRL
        benchmarkPositions = new HashSet<>();
        benchmarkPositions.add(new MaterializedPosition(100, nzdSecurity.getKey()));
        final Portfolio benchmark2 =
                new Portfolio(new PortfolioKey("testBenchmark", 1), "test", benchmarkPositions);
        // a really dumb PortfolioProvider that always returns benchmark2
        PortfolioProvider benchmark2Provider = (key -> benchmark2);

        // any concentration is infinitely higher than the benchmark, so should fail
        assertFalse(rule
                .evaluate(portfolio, benchmark2,
                        new EvaluationContext(benchmark2Provider, securityProvider, null))
                .isPassed(), "portfolio with any concentration should have failed");

        // the rule tests that the concentration of currency = BRL is at least 95% of the
        // benchmark
        rule = new ConcentrationRule(null, "test rule", c -> "BRL".equals(
                c.getPosition().getSecurity(securityProvider).getAttributeValue(TestUtil.currency)),
                0.95, null, null);

        // create a portfolio with 0% concentration in BRL
        positions = new HashSet<>();
        positions.add(new MaterializedPosition(100, nzdSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        // zero concentration is at least zero, so should pass
        assertTrue(rule
                .evaluate(portfolio, benchmark2,
                        new EvaluationContext(benchmark2Provider, securityProvider, null))
                .isPassed(), "portfolio with any concentration should have passed");

        // create a portfolio with 1% concentration in BRL
        positions = new HashSet<>();
        positions.add(new MaterializedPosition(1, brlSecurity.getKey()));
        positions.add(new MaterializedPosition(99, nzdSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        // any concentration is at least zero, so should pass
        assertTrue(rule
                .evaluate(portfolio, benchmark2,
                        new EvaluationContext(benchmark2Provider, securityProvider, null))
                .isPassed(), "portfolio with any concentration should have passed");

        // the rule tests that the concentration of currency = BRL is at most 105% of the
        // benchmark
        rule = new ConcentrationRule(null, "test rule", c -> "BRL".equals(
                c.getPosition().getSecurity(securityProvider).getAttributeValue(TestUtil.currency)),
                null, 1.05, null);

        // any concentration is infinitely higher than the benchmark, so should fail
        assertFalse(rule
                .evaluate(portfolio, benchmark2,
                        new EvaluationContext(benchmark2Provider, securityProvider, null))
                .isPassed(), "portfolio with any concentration should have failed");
    }
}
