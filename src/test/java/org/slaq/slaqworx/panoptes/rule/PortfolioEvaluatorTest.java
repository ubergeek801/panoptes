package org.slaq.slaqworx.panoptes.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

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
        private static final long serialVersionUID = 1L;

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
        private static final long serialVersionUID = 1L;

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
        private static final long serialVersionUID = 1L;

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
    public void testEvaluateAggregation() {
        Security iss1Sec1 =
                securityProvider.newSecurity("issuer1Sec1", Map.of(TestUtil.issuer, "ISSFOO"));
        Security iss1Sec2 =
                securityProvider.newSecurity("issuer1Sec2", Map.of(TestUtil.issuer, "ISSFOO"));
        Security iss1Sec3 =
                securityProvider.newSecurity("issuer1Sec3", Map.of(TestUtil.issuer, "ISSFOO"));
        Security iss2Sec1 =
                securityProvider.newSecurity("issuer2Sec1", Map.of(TestUtil.issuer, "ISSBAR"));
        Security iss2Sec2 =
                securityProvider.newSecurity("issuer2Sec2", Map.of(TestUtil.issuer, "ISSBAR"));
        Security iss3Sec1 =
                securityProvider.newSecurity("issuer3Sec1", Map.of(TestUtil.issuer, "ISSBAZ"));
        Security iss4Sec1 =
                securityProvider.newSecurity("issuer4Sec1", Map.of(TestUtil.issuer, "ISSABC"));
        Security iss4Sec2 =
                securityProvider.newSecurity("issuer4Sec2", Map.of(TestUtil.issuer, "ISSABC"));
        Security iss5Sec1 =
                securityProvider.newSecurity("issuer5Sec1", Map.of(TestUtil.issuer, "ISSDEF"));
        Security iss6Sec1 =
                securityProvider.newSecurity("issuer6Sec1", Map.of(TestUtil.issuer, "ISSGHI"));

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

        HashSet<Rule> rules = new HashSet<>();
        ConcentrationRule top2issuerRule =
                new ConcentrationRule(new RuleKey("top2", 1), "top 2 issuer concentration", null,
                        null, 0.25, new TopNSecurityAttributeAggregator(TestUtil.issuer, 2));
        rules.add(top2issuerRule);
        ConcentrationRule top3issuerRule =
                new ConcentrationRule(new RuleKey("top3", 1), "top 3 issuer concentration", null,
                        null, 0.75, new TopNSecurityAttributeAggregator(TestUtil.issuer, 3));
        rules.add(top3issuerRule);
        ConcentrationRule top10issuerRule =
                new ConcentrationRule(new RuleKey("top10", 1), "top 10 issuer concentration", null,
                        null, 0.999, new TopNSecurityAttributeAggregator(TestUtil.issuer, 10));
        rules.add(top10issuerRule);

        Portfolio portfolio = new Portfolio(null, positions, null, rules);

        PortfolioEvaluator evaluator = new PortfolioEvaluator();
        Map<Rule, Map<EvaluationGroup<?>, EvaluationResult>> allResults =
                evaluator.evaluate(portfolio, new EvaluationContext(null, securityProvider));

        assertEquals("number of results should equal number of Rules", rules.size(),
                allResults.size());

        Map<EvaluationGroup<?>, EvaluationResult> top2IssuerResults =
                allResults.get(top2issuerRule);
        Map<EvaluationGroup<?>, EvaluationResult> top3IssuerResults =
                allResults.get(top3issuerRule);
        Map<EvaluationGroup<?>, EvaluationResult> top10IssuerResults =
                allResults.get(top10issuerRule);

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

        HashSet<Rule> rules = new HashSet<>();
        rules.add(passRule);
        rules.add(failRule);
        rules.add(exceptionRule);

        Position dummyPosition = new Position(1, TestUtil.s1);
        Set<Position> dummyPositions = Set.of(dummyPosition);

        Map<Rule, Map<EvaluationGroup<?>, EvaluationResult>> results =
                new PortfolioEvaluator().evaluate(rules.stream(),
                        new Portfolio(new PortfolioKey("testPortfolio", 1), dummyPositions),
                        new EvaluationContext(null, securityProvider));
        // 3 distinct rules should result in 3 evaluations
        assertEquals("unexpected number of results", 3, results.size());
        assertTrue("always-pass rule should have passed",
                results.get(passRule).get(EvaluationGroup.defaultGroup()).isPassed());
        assertFalse("always-fail rule should have failed",
                results.get(failRule).get(EvaluationGroup.defaultGroup()).isPassed());
        assertFalse("exception-throwing rule should have failed",
                results.get(exceptionRule).get(EvaluationGroup.defaultGroup()).isPassed());
    }

    /**
     * Tests that evaluate() behaves as expected when input Positions are partitioned among multiple
     * EvaluationGroups. This also implicitly tests ConcentrationRule and WeightedAverageRule.
     */
    @Test
    public void testEvaluateGroups() {
        PortfolioEvaluator evaluator = new PortfolioEvaluator();

        Map<SecurityAttribute<?>, ? super Object> usdAttributes = Map.of(TestUtil.currency, "USD",
                TestUtil.ratingValue, 90d, TestUtil.duration, 3d, TestUtil.issuer, "ISSFOO");
        Security usdSecurity = securityProvider.newSecurity("usdSec", usdAttributes);

        Map<SecurityAttribute<?>, ? super Object> nzdAttributes = Map.of(TestUtil.currency, "NZD",
                TestUtil.ratingValue, 80d, TestUtil.duration, 4d, TestUtil.issuer, "ISSFOO");
        Security nzdSecurity = securityProvider.newSecurity("nzdSec", nzdAttributes);

        Map<SecurityAttribute<?>, ? super Object> cadAttributes = Map.of(TestUtil.currency, "CAD",
                TestUtil.ratingValue, 75d, TestUtil.duration, 5d, TestUtil.issuer, "ISSBAR");
        Security cadSecurity = securityProvider.newSecurity("cadSec", cadAttributes);

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

        HashSet<Rule> rules = new HashSet<>();
        Rule durationRule = new WeightedAverageRule(null, "currency-grouped duration rule", null,
                TestUtil.duration, null, 4d,
                new SecurityAttributeGroupClassifier(TestUtil.currency));
        rules.add(durationRule);
        Rule qualityRule = new WeightedAverageRule(null, "ungrouped quality rule", null,
                TestUtil.ratingValue, 80d, null, null);
        rules.add(qualityRule);
        Rule issuerRule = new ConcentrationRule(null, "issuer-grouped concentration rule", null,
                null, 0.5, new SecurityAttributeGroupClassifier(TestUtil.issuer));
        rules.add(issuerRule);

        // total value = 2_100, weighted rating = 165_500, weighted duration = 9_200,
        // weighted average rating = 78.80952381, weighted average duration = 4.380952381
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), positions, null, rules);

        Map<Rule, Map<EvaluationGroup<?>, EvaluationResult>> results =
                evaluator.evaluate(portfolio, new EvaluationContext(null, securityProvider));

        // all rules should have entries
        assertEquals("number of evaluated rules should match number of portfolio rules",
                rules.size(), results.size());

        // each rule's entry should have a number of results equal to the number of distinct groups
        // for that rule
        Map<EvaluationGroup<?>, EvaluationResult> durationResults = results.get(durationRule);
        assertEquals("number of duration rule results should match number of currencies", 3,
                durationResults.size());
        Map<EvaluationGroup<?>, EvaluationResult> qualityResults = results.get(qualityRule);
        assertEquals("quality rule results should have a single group", 1, qualityResults.size());
        Map<EvaluationGroup<?>, EvaluationResult> issuerResults = results.get(issuerRule);
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
    public void testEvaluateOverrides() {
        PortfolioEvaluator evaluator = new PortfolioEvaluator();

        Position dummyPosition = new Position(1, TestUtil.s1);
        Set<Position> dummyPositions = Set.of(dummyPosition);

        Position overridePosition = new Position(1, TestUtil.s2);
        Set<Position> overridePositions = Set.of(overridePosition);

        Portfolio portfolioBenchmark =
                new Portfolio(new PortfolioKey("testPortfolioBenchmark", 1), dummyPositions);
        Portfolio overrideBenchmark =
                new Portfolio(new PortfolioKey("testOverrideBenchmark", 1), overridePositions);
        // a really dumb PortfolioProvider that returns either portfolioBenchmark or
        // overrideBenchmark
        PortfolioProvider benchmarkProvider =
                k -> (portfolioBenchmark.getId().equals(k) ? portfolioBenchmark
                        : overrideBenchmark);

        DummyRule passRule = new DummyRule("testPass", true);
        DummyRule failRule = new DummyRule("testFail", false);
        UseBenchmarkRule usePortfolioBenchmarkRule =
                new UseBenchmarkRule("testBenchmarkId", portfolioBenchmark);

        HashSet<Rule> portfolioRules = new HashSet<>();
        portfolioRules.add(passRule);
        portfolioRules.add(failRule);
        portfolioRules.add(usePortfolioBenchmarkRule);

        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), dummyPositions,
                portfolioBenchmark, portfolioRules);

        // test the form of evaluate() that should use the portfolio defaults
        Map<Rule, Map<EvaluationGroup<?>, EvaluationResult>> results = evaluator.evaluate(portfolio,
                new EvaluationContext(benchmarkProvider, securityProvider));

        // 3 distinct rules should result in 3 evaluations
        assertEquals("unexpected number of results", 3, results.size());
        assertTrue("always-pass rule should have passed",
                results.get(passRule).get(EvaluationGroup.defaultGroup()).isPassed());
        assertFalse("always-fail rule should have failed",
                results.get(failRule).get(EvaluationGroup.defaultGroup()).isPassed());
        assertTrue("portfolio benchmark should have been used", results
                .get(usePortfolioBenchmarkRule).get(EvaluationGroup.defaultGroup()).isPassed());

        // test the form of evaluate() that should override the portfolio benchmark
        results = evaluator.evaluate(portfolio, overrideBenchmark,
                new EvaluationContext(benchmarkProvider, securityProvider));

        assertEquals("unexpected number of results", 3, results.size());
        assertTrue("always-pass rule should have passed",
                results.get(passRule).get(EvaluationGroup.defaultGroup()).isPassed());
        assertFalse("always-fail rule should have failed",
                results.get(failRule).get(EvaluationGroup.defaultGroup()).isPassed());
        assertFalse("override benchmark should have been used", results
                .get(usePortfolioBenchmarkRule).get(EvaluationGroup.defaultGroup()).isPassed());

        HashSet<Rule> overrideRules = new HashSet<>();
        overrideRules.add(usePortfolioBenchmarkRule);

        // test the form of evaluate() that should override the portfolio rules
        results = evaluator.evaluate(overrideRules.stream(), portfolio,
                new EvaluationContext(benchmarkProvider, securityProvider));

        assertEquals("unexpected number of results", 1, results.size());
        assertTrue("portfolio benchmark should have been used", results
                .get(usePortfolioBenchmarkRule).get(EvaluationGroup.defaultGroup()).isPassed());

        // test the form of evaluate() that should override the portfolio rules and benchmark
        results = evaluator.evaluate(overrideRules.stream(), portfolio, overrideBenchmark,
                new EvaluationContext(benchmarkProvider, securityProvider));

        assertEquals("unexpected number of results", 1, results.size());
        assertFalse("override benchmark should have been used", results
                .get(usePortfolioBenchmarkRule).get(EvaluationGroup.defaultGroup()).isPassed());
    }
}
