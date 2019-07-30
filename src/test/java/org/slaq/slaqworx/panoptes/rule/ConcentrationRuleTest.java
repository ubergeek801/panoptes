package org.slaq.slaqworx.panoptes.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
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
        ConcentrationRule rule = new ConcentrationRule(null, "test rule",
                p -> "Emerging Markets"
                        .equals(p.getSecurity(securityProvider).getAttributeValue(TestUtil.region)),
                null, 0.1, null);

        Security emergingMarketSecurity =
                securityProvider.newSecurity("s1", Map.of(TestUtil.region, "Emerging Markets"));
        Security usSecurity = securityProvider.newSecurity("s2", Map.of(TestUtil.region, "US"));

        // create a portfolio with 50% concentration in Emerging Markets
        HashSet<Position> positions = new HashSet<>();
        positions.add(new Position(100, emergingMarketSecurity));
        positions.add(new Position(100, usSecurity));
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), positions);

        assertFalse("portfolio with > 10% concentration should have failed",
                rule.evaluate(portfolio, null, new EvaluationContext(null, securityProvider))
                        .isPassed());

        // create a portfolio with 10% concentration
        positions = new HashSet<>();
        positions.add(new Position(100, emergingMarketSecurity));
        positions.add(new Position(900, usSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), positions);

        assertTrue("portfolio with == 10% concentration should have passed",
                rule.evaluate(portfolio, null, new EvaluationContext(null, securityProvider))
                        .isPassed());

        // create a portfolio with 5% concentration
        positions = new HashSet<>();
        positions.add(new Position(50, emergingMarketSecurity));
        positions.add(new Position(950, usSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), positions);

        assertTrue("portfolio with == 5% concentration should have passed",
                rule.evaluate(portfolio, null, new EvaluationContext(null, securityProvider))
                        .isPassed());
    }

    /**
     * Tests that benchmark-relative ConcentrationRule evaluation behaves as expected.
     */
    @Test
    public void testEvaluateBenchmarkRelative() {
        // the rule tests that the concentration of currency = BRL is between 95% and 105% of the
        // benchmark
        ConcentrationRule rule = new ConcentrationRule(null, "test rule",
                p -> "BRL".equals(
                        p.getSecurity(securityProvider).getAttributeValue(TestUtil.currency)),
                0.95, 1.05, null);

        Security brlSecurity = securityProvider.newSecurity("s1", Map.of(TestUtil.currency, "BRL"));
        Security nzdSecurity = securityProvider.newSecurity("s2", Map.of(TestUtil.currency, "NZD"));

        // create a benchmark with 50% concentration in BRL
        HashSet<Position> benchmarkPositions = new HashSet<>();
        benchmarkPositions.add(new Position(100, brlSecurity));
        benchmarkPositions.add(new Position(100, nzdSecurity));
        final Portfolio benchmark1 =
                new Portfolio(new PortfolioKey("testBenchmark", 1), benchmarkPositions);
        // a really dumb PortfolioProvider that always returns benchmark1
        PortfolioProvider benchmark1Provider = (key -> benchmark1);

        // create a portfolio with 56% concentration in BRL
        HashSet<Position> positions = new HashSet<>();
        positions.add(new Position(56, brlSecurity));
        positions.add(new Position(44, nzdSecurity));
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), positions);

        assertFalse(
                "portfolio with == 56% concentration should have failed", rule
                        .evaluate(portfolio, benchmark1,
                                new EvaluationContext(benchmark1Provider, securityProvider))
                        .isPassed());

        // create a portfolio with 44% concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(44, brlSecurity));
        positions.add(new Position(56, nzdSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), positions);

        assertFalse(
                "portfolio with == 44% concentration should have failed", rule
                        .evaluate(portfolio, benchmark1,
                                new EvaluationContext(benchmark1Provider, securityProvider))
                        .isPassed());

        // create a portfolio with 52.5% (50% * 105%) concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(52.5, brlSecurity));
        positions.add(new Position(47.5, nzdSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), positions);

        assertTrue(
                "portfolio with == 52.5% concentration should have passed", rule
                        .evaluate(portfolio, benchmark1,
                                new EvaluationContext(benchmark1Provider, securityProvider))
                        .isPassed());

        // create a portfolio with 47.5% (50% * 95%) concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(47.5, brlSecurity));
        positions.add(new Position(52.5, nzdSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), positions);

        assertTrue(
                "portfolio with == 52.5% concentration should have passed", rule
                        .evaluate(portfolio, benchmark1,
                                new EvaluationContext(benchmark1Provider, securityProvider))
                        .isPassed());

        // create a benchmark with 0% concentration in BRL
        benchmarkPositions = new HashSet<>();
        benchmarkPositions.add(new Position(100, nzdSecurity));
        final Portfolio benchmark2 =
                new Portfolio(new PortfolioKey("testBenchmark", 1), benchmarkPositions);
        // a really dumb PortfolioProvider that always returns benchmark2
        PortfolioProvider benchmark2Provider = (key -> benchmark2);

        // any concentration is infinitely higher than the benchmark, so should fail
        assertFalse(
                "portfolio with any concentration should have failed", rule
                        .evaluate(portfolio, benchmark2,
                                new EvaluationContext(benchmark2Provider, securityProvider))
                        .isPassed());

        // the rule tests that the concentration of currency = BRL is at least 95% of the
        // benchmark
        rule = new ConcentrationRule(null, "test rule",
                p -> "BRL".equals(
                        p.getSecurity(securityProvider).getAttributeValue(TestUtil.currency)),
                0.95, null, null);

        // create a portfolio with 0% concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(100, nzdSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), positions);

        // zero concentration is at least zero, so should pass
        assertTrue(
                "portfolio with any concentration should have passed", rule
                        .evaluate(portfolio, benchmark2,
                                new EvaluationContext(benchmark2Provider, securityProvider))
                        .isPassed());

        // create a portfolio with 1% concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(1, brlSecurity));
        positions.add(new Position(99, nzdSecurity));
        portfolio = new Portfolio(new PortfolioKey("test", 1), positions);

        // any concentration is at least zero, so should pass
        assertTrue(
                "portfolio with any concentration should have passed", rule
                        .evaluate(portfolio, benchmark2,
                                new EvaluationContext(benchmark2Provider, securityProvider))
                        .isPassed());

        // the rule tests that the concentration of currency = BRL is at most 105% of the
        // benchmark
        rule = new ConcentrationRule(null, "test rule",
                p -> "BRL".equals(
                        p.getSecurity(securityProvider).getAttributeValue(TestUtil.currency)),
                null, 1.05, null);

        // any concentration is infinitely higher than the benchmark, so should fail
        assertFalse(
                "portfolio with any concentration should have failed", rule
                        .evaluate(portfolio, benchmark2,
                                new EvaluationContext(benchmark2Provider, securityProvider))
                        .isPassed());
    }
}
