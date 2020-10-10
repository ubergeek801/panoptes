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

        assertFalse(
                rule.evaluate(portfolio, null, TestUtil.defaultTestEvaluationContext()).isPassed(),
                "portfolio with > 10% concentration should have failed");

        // create a Portfolio with 10% concentration
        positions = new HashSet<>();
        positions.add(new SimplePosition(100, emergingMarketSecurity.getKey()));
        positions.add(new SimplePosition(900, usSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertNull(rule.evaluate(portfolio, null, TestUtil.defaultTestEvaluationContext())
                .getException(), "rule should not have thrown exception");
        assertTrue(
                rule.evaluate(portfolio, null, TestUtil.defaultTestEvaluationContext()).isPassed(),
                "portfolio with == 10% concentration should have passed");

        // create a Portfolio with 5% concentration
        positions = new HashSet<>();
        positions.add(new SimplePosition(50, emergingMarketSecurity.getKey()));
        positions.add(new SimplePosition(950, usSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        assertTrue(
                rule.evaluate(portfolio, null, TestUtil.defaultTestEvaluationContext()).isPassed(),
                "portfolio with == 5% concentration should have passed");

        // repeat the first test with a GroovyPositionFilter

        rule = new ConcentrationRule(null, "test rule",
                GroovyPositionFilter.of("s.region == 'Emerging Markets'"), null, 0.1, null);

        // create a Portfolio with 50% concentration in Emerging Markets
        positions = new HashSet<>();
        positions.add(new SimplePosition(100, emergingMarketSecurity.getKey()));
        positions.add(new SimplePosition(100, usSecurity.getKey()));
        portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        ValueResult result =
                rule.evaluate(portfolio, null, TestUtil.defaultTestEvaluationContext());
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

        result = rule.evaluate(portfolio, null, TestUtil.defaultTestEvaluationContext());
        assertFalse(result.isPassed(), "portfolio with > 10% concentration should have failed");
        assertNotNull(result.getException(), "rule should have failed with exception");
    }
}
