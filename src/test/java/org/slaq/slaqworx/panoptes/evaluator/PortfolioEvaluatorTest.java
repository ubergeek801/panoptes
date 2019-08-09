package org.slaq.slaqworx.panoptes.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.AbstractRule;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
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
    private static class DummyRule extends AbstractRule {
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
    private static class ExceptionThrowingRule extends AbstractRule {
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
    private static class UseBenchmarkRule extends AbstractRule {
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
        Security iss1Sec1 = securityProvider.newSecurity(Map.of(TestUtil.issuer, "ISSFOO"));
        Security iss1Sec2 = securityProvider.newSecurity(Map.of(TestUtil.issuer, "ISSFOO"));
        Security iss1Sec3 = securityProvider.newSecurity(Map.of(TestUtil.issuer, "ISSFOO"));
        Security iss2Sec1 = securityProvider.newSecurity(Map.of(TestUtil.issuer, "ISSBAR"));
        Security iss2Sec2 = securityProvider.newSecurity(Map.of(TestUtil.issuer, "ISSBAR"));
        Security iss3Sec1 = securityProvider.newSecurity(Map.of(TestUtil.issuer, "ISSBAZ"));
        Security iss4Sec1 = securityProvider.newSecurity(Map.of(TestUtil.issuer, "ISSABC"));
        Security iss4Sec2 = securityProvider.newSecurity(Map.of(TestUtil.issuer, "ISSABC"));
        Security iss5Sec1 = securityProvider.newSecurity(Map.of(TestUtil.issuer, "ISSDEF"));
        Security iss6Sec1 = securityProvider.newSecurity(Map.of(TestUtil.issuer, "ISSGHI"));

        // the top 3 issuers are ISSFOO (300 or 30%), ISSBAR (200 or 20%), ISSABC (200 or 20%) for a
        // total of 70% concentration; the top 2 are 50% concentration
        HashSet<Position> positions = new HashSet<>();
        MaterializedPosition iss1Sec1Pos = new MaterializedPosition(100, iss1Sec1.getKey());
        positions.add(iss1Sec1Pos);
        MaterializedPosition iss1Sec2Pos = new MaterializedPosition(100, iss1Sec2.getKey());
        positions.add(iss1Sec2Pos);
        MaterializedPosition iss1Sec3Pos = new MaterializedPosition(100, iss1Sec3.getKey());
        positions.add(iss1Sec3Pos);
        MaterializedPosition iss2Sec1Pos = new MaterializedPosition(100, iss2Sec1.getKey());
        positions.add(iss2Sec1Pos);
        MaterializedPosition iss2Sec2Pos = new MaterializedPosition(100, iss2Sec2.getKey());
        positions.add(iss2Sec2Pos);
        MaterializedPosition iss3Sec1Pos = new MaterializedPosition(100, iss3Sec1.getKey());
        positions.add(iss3Sec1Pos);
        MaterializedPosition iss4Sec1Pos = new MaterializedPosition(100, iss4Sec1.getKey());
        positions.add(iss4Sec1Pos);
        MaterializedPosition iss4Sec2Pos = new MaterializedPosition(100, iss4Sec2.getKey());
        positions.add(iss4Sec2Pos);
        MaterializedPosition iss5Sec1Pos = new MaterializedPosition(100, iss5Sec1.getKey());
        positions.add(iss5Sec1Pos);
        MaterializedPosition iss6Sec1Pos = new MaterializedPosition(100, iss6Sec1.getKey());
        positions.add(iss6Sec1Pos);

        HashMap<RuleKey, Rule> rules = new HashMap<>();
        ConcentrationRule top2issuerRule =
                new ConcentrationRule(new RuleKey("top2"), "top 2 issuer concentration", null, null,
                        0.25, new TopNSecurityAttributeAggregator(TestUtil.issuer, 2));
        rules.put(top2issuerRule.getKey(), top2issuerRule);
        ConcentrationRule top3issuerRule =
                new ConcentrationRule(new RuleKey("top3"), "top 3 issuer concentration", null, null,
                        0.75, new TopNSecurityAttributeAggregator(TestUtil.issuer, 3));
        rules.put(top3issuerRule.getKey(), top3issuerRule);
        ConcentrationRule top10issuerRule =
                new ConcentrationRule(new RuleKey("top10"), "top 10 issuer concentration", null,
                        null, 0.999, new TopNSecurityAttributeAggregator(TestUtil.issuer, 10));
        rules.put(top10issuerRule.getKey(), top10issuerRule);

        RuleProvider ruleProvider = (k -> rules.get(k));

        Portfolio portfolio =
                new Portfolio(null, "test", positions, (PortfolioKey)null, rules.values());

        PortfolioEvaluator evaluator = new PortfolioEvaluator();
        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> allResults = evaluator
                .evaluate(portfolio, new EvaluationContext(null, securityProvider, ruleProvider));

        assertEquals("number of results should equal number of Rules", rules.size(),
                allResults.size());

        Map<EvaluationGroup<?>, EvaluationResult> top2IssuerResults =
                allResults.get(top2issuerRule.getKey());
        Map<EvaluationGroup<?>, EvaluationResult> top3IssuerResults =
                allResults.get(top3issuerRule.getKey());
        Map<EvaluationGroup<?>, EvaluationResult> top10IssuerResults =
                allResults.get(top10issuerRule.getKey());

        assertNotNull("should have found results for top 2 issuer rule", top2IssuerResults);
        assertNotNull("should have found results for top 3 issuer rule", top3IssuerResults);
        assertNotNull("should have found results for top 10 issuer rule", top10IssuerResults);

        // all Rules should create one group for each issuer (6) plus one for the aggregate
        assertEquals("unexpected number of groups for top 2 issuer rule", 7,
                top2IssuerResults.size());
        assertEquals("unexpected number of groups for top 3 issuer rule", 7,
                top3IssuerResults.size());
        assertEquals("unexpected number of groups for top 10 issuer rule", 7,
                top10IssuerResults.size());

        top2IssuerResults.forEach((group, result) -> {
            switch (group.getId()) {
            case "ISSFOO":
                // this concentration alone (30%) is enough to exceed the rule limit (25%)
                assertFalse("top 2 issuer rule should have failed for ISSFOO", result.isPassed());
                break;
            case "ISSBAR":
            case "ISSBAZ":
            case "ISSABC":
            case "ISSDEF":
            case "ISSGHI":
                // these concentrations are 20% or less so should pass
                assertTrue("top 2 issuer rule should have passed for " + group.getId(),
                        result.isPassed());
                break;
            default:
                // the aggregate is 50% so should fail
                assertFalse("top 2 issuer rule should have failed for aggregate",
                        result.isPassed());
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
                assertTrue("top 3 issuer rule should have passed for " + group.getId(),
                        result.isPassed());
                break;
            default:
                // the aggregate is 70% so should pass
                assertTrue("top 3 issuer rule should have passed for aggregate", result.isPassed());
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
                assertTrue("top 3 issuer rule should have passed for " + group.getId(),
                        result.isPassed());
                break;
            default:
                // since there are fewer than 10 issuers, the aggregate is 100% so should fail
                assertFalse("top 10 issuer rule should have failed for aggregate",
                        result.isPassed());
            }
        });
    }

    /**
     * Tests that evaluate() behaves as expected when an unexpected exception is thrown (expect the
     * unexpected!).
     */
    @Test
    public void testEvaluateException() {
        DummyRule passRule = new DummyRule("testPass", true);
        DummyRule failRule = new DummyRule("testFail", false);
        ExceptionThrowingRule exceptionRule = new ExceptionThrowingRule("exceptionThrowingRule");

        HashMap<RuleKey, Rule> rules = new HashMap<>();
        rules.put(passRule.getKey(), passRule);
        rules.put(failRule.getKey(), failRule);
        rules.put(exceptionRule.getKey(), exceptionRule);

        RuleProvider ruleProvider = (k -> rules.get(k));

        MaterializedPosition dummyPosition = new MaterializedPosition(1, TestUtil.s1.getKey());
        Set<Position> dummyPositions = Set.of(dummyPosition);

        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results =
                new PortfolioEvaluator().evaluate(rules.values().stream(),
                        new Portfolio(new PortfolioKey("testPortfolio", 1), "test", dummyPositions),
                        new EvaluationContext(null, securityProvider, ruleProvider));
        // 3 distinct rules should result in 3 evaluations
        assertEquals("unexpected number of results", 3, results.size());
        assertTrue("always-pass rule should have passed",
                results.get(passRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed());
        assertFalse("always-fail rule should have failed",
                results.get(failRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed());
        assertFalse("exception-throwing rule should have failed",
                results.get(exceptionRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed());
    }

    /**
     * Tests that evaluate() behaves as expected when input Positions are partitioned among multiple
     * EvaluationGroups. This also implicitly tests ConcentrationRule and WeightedAverageRule.
     */
    @Test
    public void testEvaluateGroups() throws Exception {
        PortfolioEvaluator evaluator = new PortfolioEvaluator();

        Map<SecurityAttribute<?>, ? super Object> usdAttributes = Map.of(TestUtil.currency, "USD",
                TestUtil.ratingValue, 90d, TestUtil.duration, 3d, TestUtil.issuer, "ISSFOO");
        Security usdSecurity = securityProvider.newSecurity(usdAttributes);

        Map<SecurityAttribute<?>, ? super Object> nzdAttributes = Map.of(TestUtil.currency, "NZD",
                TestUtil.ratingValue, 80d, TestUtil.duration, 4d, TestUtil.issuer, "ISSFOO");
        Security nzdSecurity = securityProvider.newSecurity(nzdAttributes);

        Map<SecurityAttribute<?>, ? super Object> cadAttributes = Map.of(TestUtil.currency, "CAD",
                TestUtil.ratingValue, 75d, TestUtil.duration, 5d, TestUtil.issuer, "ISSBAR");
        Security cadSecurity = securityProvider.newSecurity(cadAttributes);

        HashSet<Position> positions = new HashSet<>();
        // value = 100, weighted rating = 9_000, weighted duration = 300
        MaterializedPosition usdPosition1 = new MaterializedPosition(100, usdSecurity.getKey());
        positions.add(usdPosition1);
        // value = 200, weighted rating = 18_000, weighted duration = 600
        MaterializedPosition usdPosition2 = new MaterializedPosition(200, usdSecurity.getKey());
        positions.add(usdPosition2);
        // value = 300, weighted rating = 24_000, weighted duration = 1_200
        MaterializedPosition nzdPosition1 = new MaterializedPosition(300, nzdSecurity.getKey());
        positions.add(nzdPosition1);
        // value = 400, weighted rating = 32_000, weighted duration = 1_600
        MaterializedPosition nzdPosition2 = new MaterializedPosition(400, nzdSecurity.getKey());
        positions.add(nzdPosition2);
        // value = 500, weighted rating = 37_500, weighted duration = 2_500
        MaterializedPosition cadPosition1 = new MaterializedPosition(500, cadSecurity.getKey());
        positions.add(cadPosition1);
        // value = 600, weighted rating = 45_000, weighted duration = 3_000
        MaterializedPosition cadPosition2 = new MaterializedPosition(600, cadSecurity.getKey());
        positions.add(cadPosition2);

        HashMap<RuleKey, Rule> rules = new HashMap<>();
        Rule durationRule = new WeightedAverageRule(null, "currency-grouped duration rule", null,
                TestUtil.duration, null, 4d,
                new SecurityAttributeGroupClassifier(TestUtil.currency));
        rules.put(durationRule.getKey(), durationRule);
        Rule qualityRule = new WeightedAverageRule(null, "ungrouped quality rule", null,
                TestUtil.ratingValue, 80d, null, null);
        rules.put(qualityRule.getKey(), qualityRule);
        Rule issuerRule = new ConcentrationRule(null, "issuer-grouped concentration rule", null,
                null, 0.5, new SecurityAttributeGroupClassifier(TestUtil.issuer));
        rules.put(issuerRule.getKey(), issuerRule);

        RuleProvider ruleProvider = (k -> rules.get(k));

        // total value = 2_100, weighted rating = 165_500, weighted duration = 9_200,
        // weighted average rating = 78.80952381, weighted average duration = 4.380952381
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions,
                (PortfolioKey)null, rules.values());

        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results = evaluator
                .evaluate(portfolio, new EvaluationContext(null, securityProvider, ruleProvider));

        // all rules should have entries
        assertEquals("number of evaluated rules should match number of portfolio rules",
                rules.size(), results.size());

        // each rule's entry should have a number of results equal to the number of distinct groups
        // for that rule
        Map<EvaluationGroup<?>, EvaluationResult> durationResults =
                results.get(durationRule.getKey());
        assertEquals("number of duration rule results should match number of currencies", 3,
                durationResults.size());
        Map<EvaluationGroup<?>, EvaluationResult> qualityResults =
                results.get(qualityRule.getKey());
        assertEquals("quality rule results should have a single group", 1, qualityResults.size());
        Map<EvaluationGroup<?>, EvaluationResult> issuerResults = results.get(issuerRule.getKey());
        assertEquals("number of issuer rule results should match number of issuers", 2,
                issuerResults.size());

        // the duration rule is grouped by currency, so we should find results for USD, NZD, CAD;
        // USD duration = (300 + 600) / (100 + 200) = 3 which should pass
        assertTrue("duration rule should have passed for USD",
                durationResults.get(new EvaluationGroup<>("USD", null)).isPassed());
        // NZD duration = (1_200 + 1_600) / (300 + 400) = 4 which should pass
        assertTrue("duration rule should have passed for NZD",
                durationResults.get(new EvaluationGroup<>("NZD", null)).isPassed());
        // CAD duration = (2_500 + 3_000) / (500 + 600) = 5 which should fail
        assertFalse("duration rule should have faled for CAD",
                durationResults.get(new EvaluationGroup<>("CAD", null)).isPassed());

        // the quality rule is not grouped, so should have a single result for the default group
        assertFalse("quality rule should have failed",
                qualityResults.get(EvaluationGroup.defaultGroup()).isPassed());

        // the issuer rule is grouped by issuer, so we should find results for ISSFOO, ISSBAR;
        // ISSFOO concentration = (100 + 200 + 300 + 400) / 2_100 = 0.476190476 which should pass
        assertTrue("issuer rule should have passed for ISSFOO",
                issuerResults.get(new EvaluationGroup<>("ISSFOO", null)).isPassed());
        // ISSBAR concentration = (500 + 600) / 2_100 = 0.523809524 which should fail
        assertFalse("issuer rule should have failed for ISSBAR",
                issuerResults.get(new EvaluationGroup<>("ISSBAR", null)).isPassed());
    }

    /**
     * Tests that evaluate() behaves as expected when overrides are applied.
     */
    @Test
    public void testEvaluateOverrides() throws Exception {
        PortfolioEvaluator evaluator = new PortfolioEvaluator();

        MaterializedPosition dummyPosition = new MaterializedPosition(1, TestUtil.s1.getKey());
        Set<Position> dummyPositions = Set.of(dummyPosition);

        MaterializedPosition overridePosition = new MaterializedPosition(1, TestUtil.s2.getKey());
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
        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results =
                evaluator.evaluate(portfolio,
                        new EvaluationContext(benchmarkProvider, securityProvider, ruleProvider));

        // 3 distinct rules should result in 3 evaluations
        assertEquals("unexpected number of results", 3, results.size());
        assertTrue("always-pass rule should have passed",
                results.get(passRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed());
        assertFalse("always-fail rule should have failed",
                results.get(failRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed());
        assertTrue("portfolio benchmark should have been used",
                results.get(usePortfolioBenchmarkRule.getKey()).get(EvaluationGroup.defaultGroup())
                        .isPassed());

        // test the form of evaluate() that should override the portfolio benchmark
        results = evaluator.evaluate(portfolio, overrideBenchmark,
                new EvaluationContext(benchmarkProvider, securityProvider, ruleProvider));

        assertEquals("unexpected number of results", 3, results.size());
        assertTrue("always-pass rule should have passed",
                results.get(passRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed());
        assertFalse("always-fail rule should have failed",
                results.get(failRule.getKey()).get(EvaluationGroup.defaultGroup()).isPassed());
        assertFalse("override benchmark should have been used",
                results.get(usePortfolioBenchmarkRule.getKey()).get(EvaluationGroup.defaultGroup())
                        .isPassed());

        HashSet<Rule> overrideRules = new HashSet<>();
        overrideRules.add(usePortfolioBenchmarkRule);

        // test the form of evaluate() that should override the portfolio rules
        results = evaluator.evaluate(overrideRules.stream(), portfolio,
                new EvaluationContext(benchmarkProvider, securityProvider, ruleProvider));

        assertEquals("unexpected number of results", 1, results.size());
        assertTrue("portfolio benchmark should have been used",
                results.get(usePortfolioBenchmarkRule.getKey()).get(EvaluationGroup.defaultGroup())
                        .isPassed());

        // test the form of evaluate() that should override the portfolio rules and benchmark
        results = evaluator.evaluate(overrideRules.stream(), portfolio, overrideBenchmark,
                new EvaluationContext(benchmarkProvider, securityProvider, ruleProvider));

        assertEquals("unexpected number of results", 1, results.size());
        assertFalse("override benchmark should have been used",
                results.get(usePortfolioBenchmarkRule.getKey()).get(EvaluationGroup.defaultGroup())
                        .isPassed());
    }
}
