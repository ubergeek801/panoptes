package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.EligibilityListRule.EligibilityListType;

/**
 * {@code EligibilityListRuleTest} tests the functionality of the {@code EligibilityListRule}.
 *
 * @author jeremy
 */
public class EligibilityListRuleTest {
    /**
     * Tests that {@code evaluate()} behaves as expected with a blacklist.
     */
    @Test
    public void testEvaluateBlacklist() {
        Security usSecurity = TestUtil.testSecurityProvider().newSecurity("usSec", Map.of(
                SecurityAttribute.country, "US", SecurityAttribute.price, new BigDecimal("1.00")));
        Position usPosition = new Position(1, usSecurity);
        Security nzSecurity = TestUtil.testSecurityProvider().newSecurity("nzSec", Map.of(
                SecurityAttribute.country, "NZ", SecurityAttribute.price, new BigDecimal("1.00")));
        Position nzPosition = new Position(1, nzSecurity);
        Security caSecurity = TestUtil.testSecurityProvider().newSecurity("caSec", Map.of(
                SecurityAttribute.country, "CA", SecurityAttribute.price, new BigDecimal("1.00")));
        Position caPosition = new Position(1, caSecurity);

        Set<String> countryBlacklist = Set.of("US", "NZ");
        EligibilityListRule rule = new EligibilityListRule(null, "test rule", null,
                EligibilityListType.BLACKLIST, SecurityAttribute.country, countryBlacklist);

        Portfolio portfolio = new Portfolio(null, "test portfolio", Set.of(usPosition));
        RuleResult result = rule.evaluate(portfolio, null, new EvaluationContext());
        assertFalse(result.isPassed(), "Security with blacklisted country should fail");

        portfolio = new Portfolio(null, "test portfolio", Set.of(nzPosition));
        result = rule.evaluate(portfolio, null, new EvaluationContext());
        assertFalse(result.isPassed(), "Security with blacklisted country should fail");

        portfolio = new Portfolio(null, "test portfolio", Set.of(caPosition));
        result = rule.evaluate(portfolio, null, new EvaluationContext());
        assertTrue(result.isPassed(), "Security with non-blacklisted country should pass");
    }

    /**
     * Tests that {@code evaluate()} behaves as expected with a whitelist.
     */
    @Test
    public void testEvaluateWhitelist() {
        Security usSecurity = TestUtil.testSecurityProvider().newSecurity("usSec", Map.of(
                SecurityAttribute.country, "US", SecurityAttribute.price, new BigDecimal("1.00")));
        Position usPosition = new Position(1, usSecurity);
        Security nzSecurity = TestUtil.testSecurityProvider().newSecurity("nzSec", Map.of(
                SecurityAttribute.country, "NZ", SecurityAttribute.price, new BigDecimal("1.00")));
        Position nzPosition = new Position(1, nzSecurity);
        Security caSecurity = TestUtil.testSecurityProvider().newSecurity("caSec", Map.of(
                SecurityAttribute.country, "CA", SecurityAttribute.price, new BigDecimal("1.00")));
        Position caPosition = new Position(1, caSecurity);

        Set<String> assetIdWhitelist = Set.of("nzSec");
        EligibilityListRule rule = new EligibilityListRule(null, "test rule", null,
                EligibilityListType.WHITELIST, SecurityAttribute.isin, assetIdWhitelist);

        Portfolio portfolio = new Portfolio(null, "test portfolio", Set.of(usPosition));
        RuleResult result = rule.evaluate(portfolio, null, new EvaluationContext());
        assertFalse(result.isPassed(), "Security with non-whitelisted asset ID should fail");

        portfolio = new Portfolio(null, "test portfolio", Set.of(nzPosition));
        result = rule.evaluate(portfolio, null, new EvaluationContext());
        assertTrue(result.isPassed(), "Security with whitelisted asset ID should pass");

        portfolio = new Portfolio(null, "test portfolio", Set.of(caPosition));
        result = rule.evaluate(portfolio, null, new EvaluationContext());
        assertFalse(result.isPassed(), "Security with non-whitelisted asset ID should fail");
    }
}
