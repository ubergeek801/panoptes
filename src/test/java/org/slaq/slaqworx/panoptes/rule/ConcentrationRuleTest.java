package org.slaq.slaqworx.panoptes.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * ConcentrationRuleTest tests the functionality of the ConcentrationRule.
 *
 * @author jeremy
 */
public class ConcentrationRuleTest {
    /**
     * Tests that absolute ConcentrationRule evaluation behaves as expected.
     */
    @Test
    public void testEvaluate() {
        // the rule tests that the concentration of region = "Emerging Markets" <= 10%
        ConcentrationRule rule = new ConcentrationRule(null, "test rule",
                p -> "Emerging Markets"
                        .equals(p.getSecurity().getAttributeValue(SecurityAttribute.region)),
                null, 0.1, null);

        Security emergingMarketSecurity =
                new Security("s1", Map.of(SecurityAttribute.region, "Emerging Markets"));
        Security usSecurity = new Security("s2", Map.of(SecurityAttribute.region, "US"));

        // create a portfolio with 50% concentration in Emerging Markets
        HashSet<Position> positions = new HashSet<>();
        positions.add(new Position(100, emergingMarketSecurity));
        positions.add(new Position(100, usSecurity));
        Portfolio portfolio = new Portfolio("test", positions);

        assertFalse("portfolio with > 10% concentration should have failed",
                rule.evaluate(portfolio, null));

        // create a portfolio with 10% concentration
        positions = new HashSet<>();
        positions.add(new Position(100, emergingMarketSecurity));
        positions.add(new Position(900, usSecurity));
        portfolio = new Portfolio("test", positions);

        assertTrue("portfolio with == 10% concentration should have passed",
                rule.evaluate(portfolio, null));

        // create a portfolio with 5% concentration
        positions = new HashSet<>();
        positions.add(new Position(50, emergingMarketSecurity));
        positions.add(new Position(950, usSecurity));
        portfolio = new Portfolio("test", positions);

        assertTrue("portfolio with == 5% concentration should have passed",
                rule.evaluate(portfolio, null));
    }

    /**
     * Tests that benchmark-relative ConcentrationRule evaluation behaves as expected.
     */
    @Test
    public void testEvaluateBenchmarkRelative() {
        // the rule tests that the concentration of currency = BRL is between 95% and 105% of the
        // benchmark
        ConcentrationRule rule = new ConcentrationRule(null, "test rule",
                p -> "BRL".equals(p.getSecurity().getAttributeValue(SecurityAttribute.currency)),
                0.95, 1.05, null);

        Security brlSecurity = new Security("s1", Map.of(SecurityAttribute.currency, "BRL"));
        Security nzdSecurity = new Security("s2", Map.of(SecurityAttribute.currency, "NZD"));

        // create a benchmark with 50% concentration in BRL
        HashSet<Position> benchmarkPositions = new HashSet<>();
        benchmarkPositions.add(new Position(100, brlSecurity));
        benchmarkPositions.add(new Position(100, nzdSecurity));
        Portfolio benchmark = new Portfolio("testBenchmark", benchmarkPositions);

        // create a portfolio with 56% concentration in BRL
        HashSet<Position> positions = new HashSet<>();
        positions.add(new Position(56, brlSecurity));
        positions.add(new Position(44, nzdSecurity));
        Portfolio portfolio = new Portfolio("test", positions);

        assertFalse("portfolio with == 56% concentration should have failed",
                rule.evaluate(portfolio, benchmark));

        // create a portfolio with 44% concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(44, brlSecurity));
        positions.add(new Position(56, nzdSecurity));
        portfolio = new Portfolio("test", positions);

        assertFalse("portfolio with == 44% concentration should have failed",
                rule.evaluate(portfolio, benchmark));

        // create a portfolio with 52.5% (50% * 105%) concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(52.5, brlSecurity));
        positions.add(new Position(47.5, nzdSecurity));
        portfolio = new Portfolio("test", positions);

        assertTrue("portfolio with == 52.5% concentration should have passed",
                rule.evaluate(portfolio, benchmark));

        // create a portfolio with 47.5% (50% * 95%) concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(47.5, brlSecurity));
        positions.add(new Position(52.5, nzdSecurity));
        portfolio = new Portfolio("test", positions);

        assertTrue("portfolio with == 52.5% concentration should have passed",
                rule.evaluate(portfolio, benchmark));

        // create a benchmark with 0% concentration in BRL
        benchmarkPositions = new HashSet<>();
        benchmarkPositions.add(new Position(100, nzdSecurity));
        benchmark = new Portfolio("testBenchmark", benchmarkPositions);

        // any concentration is infinitely higher than the benchmark, so should fail
        assertFalse("portfolio with any concentration should have failed",
                rule.evaluate(portfolio, benchmark));

        // the rule tests that the concentration of currency = BRL is at least 95% of the
        // benchmark
        rule = new ConcentrationRule(null, "test rule",
                p -> "BRL".equals(p.getSecurity().getAttributeValue(SecurityAttribute.currency)),
                0.95, null, null);

        // create a portfolio with 0% concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(100, nzdSecurity));
        portfolio = new Portfolio("test", positions);

        // zero concentration is at least zero, so should pass
        assertTrue("portfolio with any concentration should have passed",
                rule.evaluate(portfolio, benchmark));

        // create a portfolio with 1% concentration in BRL
        positions = new HashSet<>();
        positions.add(new Position(1, brlSecurity));
        positions.add(new Position(99, nzdSecurity));
        portfolio = new Portfolio("test", positions);

        // any concentration is at least zero, so should pass
        assertTrue("portfolio with any concentration should have passed",
                rule.evaluate(portfolio, benchmark));

        // the rule tests that the concentration of currency = BRL is at most 105% of the
        // benchmark
        rule = new ConcentrationRule(null, "test rule",
                p -> "BRL".equals(p.getSecurity().getAttributeValue(SecurityAttribute.currency)),
                null, 1.05, null);

        // any concentration is infinitely higher than the benchmark, so should fail
        assertFalse("portfolio with any concentration should have failed",
                rule.evaluate(portfolio, benchmark));
    }
}
