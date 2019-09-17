package org.slaq.slaqworx.panoptes.evaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.rule.SecurityAttributeGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.TopNSecurityAttributeAggregator;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;

/**
 * PortfolioEvaluatorTest tests the functionality of the PortfolioEvaluator.
 *
 * @author jeremy
 */
public class PortfolioEvaluatorTest {
    /**
     * DummyRule facilitates testing rule evaluation behavior by always passing or always failing.
     */
    private static class DummyRule extends Rule {
        private final boolean isPass;

        public DummyRule(String description, boolean isPass) {
            super(description);
            this.isPass = isPass;
        }

        @Override
        public EvaluationResult eval(PositionSupplier portfolioPositions,
                PositionSupplier benchmarkPositions, EvaluationContext evaluationContext) {
            return new EvaluationResult(isPass);
        }
    }

    /**
     * ExceptionThrowingRule facilitates testing rule evaluation behavior by always throwing a
     * runtime exception.
     */
    private static class ExceptionThrowingRule extends Rule {
        public ExceptionThrowingRule(String description) {
            super(description);
        }

        @Override
        protected EvaluationResult eval(PositionSupplier portfolioPositions,
                PositionSupplier benchmarkPositions, EvaluationContext evaluationContext) {
            throw new RuntimeException("exception test");
        }
    }

    /**
     * UseBenchmarkRule facilitates testing whether a specific benchmark was used during a rule
     * evaluation.
     */
    private static class UseBenchmarkRule extends Rule {
        private final Portfolio benchmark;

        public UseBenchmarkRule(String description, Portfolio benchmark) {
            super(description);
            this.benchmark = benchmark;
        }

        @Override
        public EvaluationResult eval(PositionSupplier portfolioPositions,
                PositionSupplier benchmarkPositions, EvaluationContext evaluationContext) {
            return new EvaluationResult(benchmark.equals(benchmarkPositions.getPortfolio()));
        }
    }

    private TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

    /**
     * Tests that evaluate() behaves as expected with GroupAggregators (also implicitly tests
     * TopNSecurityAttributeAggregator).
     */
    @Test
    public void testEvaluateAggregation() throws Exception {
        Security iss1Sec1 =
                securityProvider.newSecurity("iss1Sec1", Map.of(SecurityAttribute.issuer, "ISSFOO",
                        SecurityAttribute.price, new BigDecimal("1.00")));
        Security iss1Sec2 =
                securityProvider.newSecurity("iss1Sec2", Map.of(SecurityAttribute.issuer, "ISSFOO",
                        SecurityAttribute.price, new BigDecimal("1.00")));
        Security iss1Sec3 =
                securityProvider.newSecurity("iss1Sec3", Map.of(SecurityAttribute.issuer, "ISSFOO",
                        SecurityAttribute.price, new BigDecimal("1.00")));
        Security iss2Sec1 =
                securityProvider.newSecurity("iss2Sec1", Map.of(SecurityAttribute.issuer, "ISSBAR",
                        SecurityAttribute.price, new BigDecimal("1.00")));
        Security iss2Sec2 =
                securityProvider.newSecurity("iss2Sec2", Map.of(SecurityAttribute.issuer, "ISSBAR",
                        SecurityAttribute.price, new BigDecimal("1.00")));
        Security iss3Sec1 =
                securityProvider.newSecurity("iss3Sec1", Map.of(SecurityAttribute.issuer, "ISSBAZ",
                        SecurityAttribute.price, new BigDecimal("1.00")));
        Security iss4Sec1 =
                securityProvider.newSecurity("iss4Sec1", Map.of(SecurityAttribute.issuer, "ISSABC",
                        SecurityAttribute.price, new BigDecimal("1.00")));
        Security iss4Sec2 =
                securityProvider.newSecurity("iss4Sec2", Map.of(SecurityAttribute.issuer, "ISSABC",
                        SecurityAttribute.price, new BigDecimal("1.00")));
        Security iss5Sec1 =
                securityProvider.newSecurity("iss5Sec1", Map.of(SecurityAttribute.issuer, "ISSDEF",
                        SecurityAttribute.price, new BigDecimal("1.00")));
        Security iss6Sec1 =
                securityProvider.newSecurity("iss6Sec1", Map.of(SecurityAttribute.issuer, "ISSGHI",
                        SecurityAttribute.price, new BigDecimal("1.00")));

        // the top 3 issuers are ISSFOO (300 or 30%), ISSBAR (200 or 20%), ISSABC (200 or 20%) for a
        // total of 70% concentration; the top 2 are 50% concentration
        HashSet<Position> positions = new HashSet<>();
        Position iss1Sec1Pos = new Position(100, iss1Sec1);
        positions.add(iss1Sec1Pos);
        Position iss1Sec2Pos = new Position(100, iss1Sec2);
        positions.add(iss1Sec2Pos);
        Position iss1Sec3Pos = new Position(100, iss1Sec3);
        positions.add(iss1Sec3Pos);
        Position iss2Sec1Pos = new Position(100, iss2Sec1);
        positions.add(iss2Sec1Pos);
        Position iss2Sec2Pos = new Position(100, iss2Sec2);
        positions.add(iss2Sec2Pos);
        Position iss3Sec1Pos = new Position(100, iss3Sec1);
        positions.add(iss3Sec1Pos);
        Position iss4Sec1Pos = new Position(100, iss4Sec1);
        positions.add(iss4Sec1Pos);
        Position iss4Sec2Pos = new Position(100, iss4Sec2);
        positions.add(iss4Sec2Pos);
        Position iss5Sec1Pos = new Position(100, iss5Sec1);
        positions.add(iss5Sec1Pos);
        Position iss6Sec1Pos = new Position(100, iss6Sec1);
        positions.add(iss6Sec1Pos);

        HashMap<RuleKey, ConfigurableRule> rules = new HashMap<>();
        ConcentrationRule top2issuerRule =
                new ConcentrationRule(new RuleKey("top2"), "top 2 issuer concentration", null, null,
                        0.25, new TopNSecurityAttributeAggregator(SecurityAttribute.issuer, 2));
        rules.put(top2issuerRule.getKey(), top2issuerRule);
        ConcentrationRule top3issuerRule =
                new ConcentrationRule(new RuleKey("top3"), "top 3 issuer concentration", null, null,
                        0.75, new TopNSecurityAttributeAggregator(SecurityAttribute.issuer, 3));
        rules.put(top3issuerRule.getKey(), top3issuerRule);
        ConcentrationRule top10issuerRule = new ConcentrationRule(new RuleKey("top10"),
                "top 10 issuer concentration", null, null, 0.999,
                new TopNSecurityAttributeAggregator(SecurityAttribute.issuer, 10));
        rules.put(top10issuerRule.getKey(), top10issuerRule);

        RuleProvider ruleProvider = (k -> rules.get(k));

        Portfolio portfolio =
                new Portfolio(null, "test", positions, (PortfolioKey)null, rules.values());

        LocalPortfolioEvaluator evaluator = new LocalPortfolioEvaluator();
        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> allResults = evaluator
                .evaluate(portfolio, new EvaluationContext(null, securityProvider, ruleProvider))
                .get();

        assertEquals(rules.size(), allResults.size(),
                "number of results should equal number of Rules");

        Map<EvaluationGroup<?>, EvaluationResult> top2IssuerResults =
                allResults.get(top2issuerRule.getKey());
        Map<EvaluationGroup<?>, EvaluationResult> top3IssuerResults =
                allResults.get(top3issuerRule.getKey());
        Map<EvaluationGroup<?>, EvaluationResult> top10IssuerResults =
                allResults.get(top10issuerRule.getKey());

        assertNotNull(top2IssuerResults, "should have found results for top 2 issuer rule");
        assertNotNull(top3IssuerResults, "should have found results for top 3 issuer rule");
        assertNotNull(top10IssuerResults, "should have found results for top 10 issuer rule");

        // all Rules should create one group for each issuer (6) plus one for the aggregate
        assertEquals(7, top2IssuerResults.size(),
                "unexpected number of groups for top 2 issuer rule");
        assertEquals(7, top3IssuerResults.size(),
                "unexpected number of groups for top 3 issuer rule");
        assertEquals(7, top10IssuerResults.size(),
                "unexpected number of groups for top 10 issuer rule");

        top2IssuerResults.forEach((group, result) -> {
            switch (group.getId()) {
            case "ISSFOO":
                // this concentration alone (30%) is enough to exceed the rule limit (25%)
                assertFalse(result.isPassed(), "top 2 issuer rule should have failed for ISSFOO");
                break;
            case "ISSBAR":
            case "ISSBAZ":
            case "ISSABC":
            case "ISSDEF":
            case "ISSGHI":
                // these concentrations are 20% or less so should pass
                assertTrue(result.isPassed(),
                        "top 2 issuer rule should have passed for " + group.getId());
                break;
            default:
                // the aggregate is 50% so should fail
                assertFalse(result.isPassed(),
                        "top 2 issuer rule should have failed for aggregate");
            }
        });

        top3IssuerResults.forEach((group, result) -> {
            switch (group.getId()) {
            case "ISSFOO":
            case "ISSBAR":
            case "ISSBAZ":
            case "ISSABC":
            case "ISSDEF":
            case "ISSGHI":
                // none of the concentrations are above 30% so should pass the 75% limit
                assertTrue(result.isPassed(),
                        "top 3 issuer rule should have passed for " + group.getId());
                break;
            default:
                // the aggregate is 70% so should pass
                assertTrue(result.isPassed(), "top 3 issuer rule should have passed for aggregate");
            }
        });

        top10IssuerResults.forEach((group, result) -> {
            switch (group.getId()) {
            case "ISSFOO":
            case "ISSBAR":
            case "ISSBAZ":
            case "ISSABC":
            case "ISSDEF":
            case "ISSGHI":
                // none of the concentrations are above 30% so should pass the 99.9% limit
                assertTrue(result.isPassed(),
                        "top 3 issuer rule should have passed for " + group.getId());
                break;
            default:
                // since there are fewer than 10 issuers, the aggregate is 100% so should fail
                assertFalse(result.isPassed(),
                        "top 10 issuer rule should have failed for aggregate");
            }
        });
    }

    /**
     * Tests that evaluate() behaves as expected when an unexpected exception is thrown (expect the
     * unexpected!).
     */
    @Test
    public void testEvaluateException() throws Exception {
        DummyRule passRule = new DummyRule("testPass", true);
        DummyRule failRule = new DummyRule("testFail", false);
        ExceptionThrowingRule exceptionRule = new ExceptionThrowingRule("exceptionThrowingRule");

        HashMap<RuleKey, Rule> rules = new HashMap<>();
        rules.put(passRule.getKey(), passRule);
        rules.put(failRule.getKey(), failRule);
        rules.put(exceptionRule.getKey(), exceptionRule);

        RuleProvider ruleProvider = (k -> rules.get(k));

        Position dummyPosition = new Position(1, TestUtil.s1);
        Set<Position> dummyPositions = Set.of(dummyPosition);

        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results =
                new LocalPortfolioEvaluator().evaluate(rules.values().stream(),
                        new Portfolio(new PortfolioKey("testPortfolio", 1), "test", dummyPositions),
                        new EvaluationContext(null, securityProvider, ruleProvider));
        // 3 distinct rules should result in 3 evaluations
        assertEquals(3, results.size(), "unexpected number of results");
        assertTrue(results.get(passRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed(),
                "always-pass rule should have passed");
        assertFalse(results.get(failRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed(),
                "always-fail rule should have failed");
        assertFalse(
                results.get(exceptionRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed(),
                "exception-throwing rule should have failed");
    }

    /**
     * Tests that evaluate() behaves as expected when input Positions are partitioned among multiple
     * EvaluationGroups. This also implicitly tests ConcentrationRule and WeightedAverageRule.
     */
    @Test
    public void testEvaluateGroups() throws Exception {
        LocalPortfolioEvaluator evaluator = new LocalPortfolioEvaluator();

        Map<SecurityAttribute<?>, ? super Object> usdAttributes =
                Map.of(SecurityAttribute.currency, "USD", SecurityAttribute.ratingValue, 90d,
                        SecurityAttribute.duration, 3d, SecurityAttribute.issuer, "ISSFOO",
                        SecurityAttribute.price, new BigDecimal("1.00"));
        Security usdSecurity = securityProvider.newSecurity("usd", usdAttributes);

        Map<SecurityAttribute<?>, ? super Object> nzdAttributes =
                Map.of(SecurityAttribute.currency, "NZD", SecurityAttribute.ratingValue, 80d,
                        SecurityAttribute.duration, 4d, SecurityAttribute.issuer, "ISSFOO",
                        SecurityAttribute.price, new BigDecimal("1.00"));
        Security nzdSecurity = securityProvider.newSecurity("nzd", nzdAttributes);

        Map<SecurityAttribute<?>, ? super Object> cadAttributes =
                Map.of(SecurityAttribute.currency, "CAD", SecurityAttribute.ratingValue, 75d,
                        SecurityAttribute.duration, 5d, SecurityAttribute.issuer, "ISSBAR",
                        SecurityAttribute.price, new BigDecimal("1.00"));
        Security cadSecurity = securityProvider.newSecurity("cad", cadAttributes);

        HashSet<Position> positions = new HashSet<>();
        // value = 100, weighted rating = 9_000, weighted duration = 300
        Position usdPosition1 = new Position(100, usdSecurity);
        positions.add(usdPosition1);
        // value = 200, weighted rating = 18_000, weighted duration = 600
        Position usdPosition2 = new Position(200, usdSecurity);
        positions.add(usdPosition2);
        // value = 300, weighted rating = 24_000, weighted duration = 1_200
        Position nzdPosition1 = new Position(300, nzdSecurity);
        positions.add(nzdPosition1);
        // value = 400, weighted rating = 32_000, weighted duration = 1_600
        Position nzdPosition2 = new Position(400, nzdSecurity);
        positions.add(nzdPosition2);
        // value = 500, weighted rating = 37_500, weighted duration = 2_500
        Position cadPosition1 = new Position(500, cadSecurity);
        positions.add(cadPosition1);
        // value = 600, weighted rating = 45_000, weighted duration = 3_000
        Position cadPosition2 = new Position(600, cadSecurity);
        positions.add(cadPosition2);

        HashMap<RuleKey, ConfigurableRule> rules = new HashMap<>();
        ConfigurableRule durationRule = new WeightedAverageRule(null,
                "currency-grouped duration rule", null, SecurityAttribute.duration, null, 4d,
                new SecurityAttributeGroupClassifier(SecurityAttribute.currency));
        rules.put(durationRule.getKey(), durationRule);
        ConfigurableRule qualityRule = new WeightedAverageRule(null, "ungrouped quality rule", null,
                SecurityAttribute.ratingValue, 80d, null, null);
        rules.put(qualityRule.getKey(), qualityRule);
        ConfigurableRule issuerRule =
                new ConcentrationRule(null, "issuer-grouped concentration rule", null, null, 0.5,
                        new SecurityAttributeGroupClassifier(SecurityAttribute.issuer));
        rules.put(issuerRule.getKey(), issuerRule);

        RuleProvider ruleProvider = (k -> rules.get(k));

        // total value = 2_100, weighted rating = 165_500, weighted duration = 9_200,
        // weighted average rating = 78.80952381, weighted average duration = 4.380952381
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions,
                (PortfolioKey)null, rules.values());

        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results = evaluator
                .evaluate(portfolio, new EvaluationContext(null, securityProvider, ruleProvider))
                .get();

        // all rules should have entries
        assertEquals(rules.size(), results.size(),
                "number of evaluated rules should match number of portfolio rules");

        // each rule's entry should have a number of results equal to the number of distinct groups
        // for that rule
        Map<EvaluationGroup<?>, EvaluationResult> durationResults =
                results.get(durationRule.getKey());
        assertEquals(3, durationResults.size(),
                "number of duration rule results should match number of currencies");
        Map<EvaluationGroup<?>, EvaluationResult> qualityResults =
                results.get(qualityRule.getKey());
        assertEquals(1, qualityResults.size(), "quality rule results should have a single group");
        Map<EvaluationGroup<?>, EvaluationResult> issuerResults = results.get(issuerRule.getKey());
        assertEquals(2, issuerResults.size(),
                "number of issuer rule results should match number of issuers");

        // the duration rule is grouped by currency, so we should find results for USD, NZD, CAD;
        // USD duration = (300 + 600) / (100 + 200) = 3 which should pass
        assertTrue(durationResults.get(new EvaluationGroup<>("USD", null)).isPassed(),
                "duration rule should have passed for USD");
        // NZD duration = (1_200 + 1_600) / (300 + 400) = 4 which should pass
        assertTrue(durationResults.get(new EvaluationGroup<>("NZD", null)).isPassed(),
                "duration rule should have passed for NZD");
        // CAD duration = (2_500 + 3_000) / (500 + 600) = 5 which should fail
        assertFalse(durationResults.get(new EvaluationGroup<>("CAD", null)).isPassed(),
                "duration rule should have failed for CAD");

        // the quality rule is not grouped, so should have a single result for the default group
        assertFalse(qualityResults.get(EvaluationGroup.defaultGroup()).isPassed(),
                "quality rule should have failed");

        // the issuer rule is grouped by issuer, so we should find results for ISSFOO, ISSBAR;
        // ISSFOO concentration = (100 + 200 + 300 + 400) / 2_100 = 0.476190476 which should pass
        assertTrue(issuerResults.get(new EvaluationGroup<>("ISSFOO", null)).isPassed(),
                "issuer rule should have passed for ISSFOO");
        // ISSBAR concentration = (500 + 600) / 2_100 = 0.523809524 which should fail
        assertFalse(issuerResults.get(new EvaluationGroup<>("ISSBAR", null)).isPassed(),
                "issuer rule should have failed for ISSBAR");
    }

    /**
     * Tests that evaluate() behaves as expected when overrides are applied.
     */
    @Test
    public void testEvaluateOverrides() throws Exception {
        LocalPortfolioEvaluator evaluator = new LocalPortfolioEvaluator();

        Position dummyPosition = new Position(1, TestUtil.s1);
        Set<Position> dummyPositions = Set.of(dummyPosition);

        Position overridePosition = new Position(1, TestUtil.s2);
        Set<Position> overridePositions = Set.of(overridePosition);

        Portfolio portfolioBenchmark = new Portfolio(new PortfolioKey("testPortfolioBenchmark", 1),
                "test", dummyPositions);
        Portfolio overrideBenchmark = new Portfolio(new PortfolioKey("testOverrideBenchmark", 1),
                "test", overridePositions);
        // a really dumb PortfolioProvider that returns either portfolioBenchmark or
        // overrideBenchmark
        PortfolioProvider benchmarkProvider =
                (k -> (portfolioBenchmark.getKey().equals(k) ? portfolioBenchmark
                        : overrideBenchmark));

        DummyRule passRule = new DummyRule("testPass", true);
        DummyRule failRule = new DummyRule("testFail", false);
        UseBenchmarkRule usePortfolioBenchmarkRule =
                new UseBenchmarkRule("testBenchmarkId", portfolioBenchmark);

        HashMap<RuleKey, Rule> portfolioRules = new HashMap<>();
        portfolioRules.put(passRule.getKey(), passRule);
        portfolioRules.put(failRule.getKey(), failRule);
        portfolioRules.put(usePortfolioBenchmarkRule.getKey(), usePortfolioBenchmarkRule);

        RuleProvider ruleProvider = (k -> portfolioRules.get(k));

        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", dummyPositions,
                portfolioBenchmark, portfolioRules.values());

        // test the form of evaluate() that should use the portfolio defaults
        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results = evaluator
                .evaluate(portfolio,
                        new EvaluationContext(benchmarkProvider, securityProvider, ruleProvider))
                .get();

        // 3 distinct rules should result in 3 evaluations
        assertEquals(3, results.size(), "unexpected number of results");
        assertTrue(results.get(passRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed(),
                "always-pass rule should have passed");
        assertFalse(results.get(failRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed(),
                "always-fail rule should have failed");
        assertTrue(results.get(usePortfolioBenchmarkRule.getKey())
                .get(EvaluationGroup.defaultGroup()).isPassed(),
                "portfolio benchmark should have been used");

        // test the form of evaluate() that should override the portfolio benchmark
        results = evaluator.evaluate(portfolio, overrideBenchmark,
                new EvaluationContext(benchmarkProvider, securityProvider, ruleProvider));

        assertEquals(3, results.size(), "unexpected number of results");
        assertTrue(results.get(passRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed(),
                "always-pass rule should have passed");
        assertFalse(results.get(failRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed(),
                "always-fail rule should have failed");
        assertFalse(results.get(usePortfolioBenchmarkRule.getKey())
                .get(EvaluationGroup.defaultGroup()).isPassed(),
                "override benchmark should have been used");

        HashSet<Rule> overrideRules = new HashSet<>();
        overrideRules.add(usePortfolioBenchmarkRule);

        // test the form of evaluate() that should override the portfolio rules
        results = evaluator.evaluate(overrideRules.stream(), portfolio,
                new EvaluationContext(benchmarkProvider, securityProvider, ruleProvider));

        assertEquals(1, results.size(), "unexpected number of results");
        assertTrue(results.get(usePortfolioBenchmarkRule.getKey())
                .get(EvaluationGroup.defaultGroup()).isPassed(),
                "portfolio benchmark should have been used");

        // test the form of evaluate() that should override the portfolio rules and benchmark
        results = evaluator.evaluate(overrideRules.stream(), portfolio, overrideBenchmark,
                new EvaluationContext(benchmarkProvider, securityProvider, ruleProvider));

        assertEquals(1, results.size(), "unexpected number of results");
        assertFalse(results.get(usePortfolioBenchmarkRule.getKey())
                .get(EvaluationGroup.defaultGroup()).isPassed(),
                "override benchmark should have been used");
    }
}
