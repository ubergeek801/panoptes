package org.slaq.slaqworx.panoptes.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * RuleEvaluatorTest tests the functionality of the RuleEvaluator.
 *
 * @author jeremy
 */
public class RuleEvaluatorTest {
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
        public boolean eval(PositionSupplier portfolioPositions,
                PositionSupplier benchmarkPositions) {
            return isPass;
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
        protected boolean eval(PositionSupplier portfolioPositions,
                PositionSupplier benchmarkPositions) {
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
        public boolean eval(PositionSupplier portfolioPositions,
                PositionSupplier benchmarkPositions) {
            return benchmark.equals(benchmarkPositions.getPortfolio());
        }
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

        Map<Rule, Map<EvaluationGroup, Boolean>> results = new RuleEvaluator()
                .evaluate(rules.stream(), new Portfolio("testPortfolio", dummyPositions));
        // 3 distinct rules should result in 3 evaluations
        assertEquals("unexpected number of results", 3, results.size());
        assertTrue("always-pass rule should have passed",
                results.get(passRule).get(EvaluationGroup.defaultGroup()));
        assertFalse("always-fail rule should have failed",
                results.get(failRule).get(EvaluationGroup.defaultGroup()));
        assertFalse("exception-throwing rule should have failed",
                results.get(exceptionRule).get(EvaluationGroup.defaultGroup()));
    }

    /**
     * Tests that evaluate() behaves as expected when input Positions are partitioned among multiple
     * EvaluationGroups. This also implicitly tests ConcentrationRule and WeightedAverageRule.
     */
    @Test
    public void testEvaluateGroups() {
        RuleEvaluator evaluator = new RuleEvaluator();

        Map<SecurityAttribute<?>, ? super Object> usdAttributes =
                Map.of(SecurityAttribute.currency, "USD", SecurityAttribute.ratingValue, 90d,
                        SecurityAttribute.duration, 3d, SecurityAttribute.issuer, "ISSFOO");
        Security usdSecurity = new Security("usdSec", usdAttributes);

        Map<SecurityAttribute<?>, ? super Object> nzdAttributes =
                Map.of(SecurityAttribute.currency, "NZD", SecurityAttribute.ratingValue, 80d,
                        SecurityAttribute.duration, 4d, SecurityAttribute.issuer, "ISSFOO");
        Security nzdSecurity = new Security("nzdSec", nzdAttributes);

        Map<SecurityAttribute<?>, ? super Object> cadAttributes =
                Map.of(SecurityAttribute.currency, "CAD", SecurityAttribute.ratingValue, 75d,
                        SecurityAttribute.duration, 5d, SecurityAttribute.issuer, "ISSBAR");
        Security cadSecurity = new Security("cadSec", cadAttributes);

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
        Rule durationRule =
                new WeightedAverageRule(null, "currency-grouped duration rule", null,
                        SecurityAttribute.duration, null, 4d,
                        p -> EvaluationGroup
                                .of(p.getSecurity().getAttributeValue(SecurityAttribute.currency)),
                        null);
        rules.add(durationRule);
        Rule qualityRule = new WeightedAverageRule(null, "ungrouped quality rule", null,
                SecurityAttribute.ratingValue, 80d, null, null, null);
        rules.add(qualityRule);
        Rule issuerRule =
                new ConcentrationRule(null, "issuer-grouped concentration rule", null, null, 0.5,
                        p -> EvaluationGroup
                                .of(p.getSecurity().getAttributeValue(SecurityAttribute.issuer)),
                        null);
        rules.add(issuerRule);

        // total value = 2_100, weighted rating = 165_500, weighted duration = 9_200,
        // weighted average rating = 78.80952381, weighted average duration = 4.380952381
        Portfolio portfolio = new Portfolio("test", positions, null, rules);

        Map<Rule, Map<EvaluationGroup, Boolean>> results = evaluator.evaluate(portfolio);

        // all rules should have entries
        assertEquals("number of evaluated rules should match number of portfolio rules",
                rules.size(), results.size());

        // each rule's entry should have a number of results equal to the number of distinct groups
        // for that rule
        Map<EvaluationGroup, Boolean> durationResults = results.get(durationRule);
        assertEquals("number of duration rule results should match number of currencies", 3,
                durationResults.size());
        Map<EvaluationGroup, Boolean> qualityResults = results.get(qualityRule);
        assertEquals("quality rule results should have a single group", 1, qualityResults.size());
        Map<EvaluationGroup, Boolean> issuerResults = results.get(issuerRule);
        assertEquals("number of issuer rule results should match number of issuers", 2,
                issuerResults.size());

        // the duration rule is grouped by currency, so we should find results for USD, NZD, CAD;
        // USD duration = (300 + 600) / (100 + 200) = 3 which should pass
        assertTrue("duration rule should have passed for USD",
                durationResults.get(EvaluationGroup.of("USD")));
        // NZD duration = (1_200 + 1_600) / (300 + 400) = 4 which should pass
        assertTrue("duration rule should have passed for NZD",
                durationResults.get(EvaluationGroup.of("NZD")));
        // CAD duration = (2_500 + 3_000) / (500 + 600) = 5 which should fail
        assertFalse("duration rule should have faled for CAD",
                durationResults.get(EvaluationGroup.of("CAD")));

        // the quality rule is not grouped, so should have a single result for the default group
        assertFalse("quality rule should have failed",
                qualityResults.get(EvaluationGroup.defaultGroup()));

        // the issuer rule is grouped by issuer, so we should find results for ISSFOO, ISSBAR;
        // ISSFOO concentration = (100 + 200 + 300 + 400) / 2_100 = 0.476190476 which should pass
        assertTrue("issuer rule should have passed for ISSFOO",
                issuerResults.get(EvaluationGroup.of("ISSFOO")));
        // ISSBAR concentration = (500 + 600) / 2_100 = 0.523809524 which should fail
        assertFalse("issuer rule should have failed for ISSBAR",
                issuerResults.get(EvaluationGroup.of("ISSBAR")));
    }

    /**
     * Tests that evaluate() behaves as expected when overrides are applied.
     */
    @Test
    public void testEvaluateOverrides() {
        RuleEvaluator evaluator = new RuleEvaluator();

        Position dummyPosition = new Position(1, TestUtil.s1);
        Set<Position> dummyPositions = Set.of(dummyPosition);

        Position overridePosition = new Position(1, TestUtil.s2);
        Set<Position> overridePositions = Set.of(overridePosition);

        Portfolio portfolioBenchmark = new Portfolio("testPortfolioBenchmark", dummyPositions);
        Portfolio overrideBenchmark = new Portfolio("testOverrideBenchmark", overridePositions);

        DummyRule passRule = new DummyRule("testPass", true);
        DummyRule failRule = new DummyRule("testFail", false);
        UseBenchmarkRule usePortfolioBenchmarkRule =
                new UseBenchmarkRule("testBenchmarkId", portfolioBenchmark);

        HashSet<Rule> portfolioRules = new HashSet<>();
        portfolioRules.add(passRule);
        portfolioRules.add(failRule);
        portfolioRules.add(usePortfolioBenchmarkRule);

        Portfolio portfolio =
                new Portfolio("test", dummyPositions, portfolioBenchmark, portfolioRules);

        // test the form of evaluate() that should use the portfolio defaults
        Map<Rule, Map<EvaluationGroup, Boolean>> results = evaluator.evaluate(portfolio);

        // 3 distinct rules should result in 3 evaluations
        assertEquals("unexpected number of results", 3, results.size());
        assertTrue("always-pass rule should have passed",
                results.get(passRule).get(EvaluationGroup.defaultGroup()));
        assertFalse("always-fail rule should have failed",
                results.get(failRule).get(EvaluationGroup.defaultGroup()));
        assertTrue("portfolio benchmark should have been used",
                results.get(usePortfolioBenchmarkRule).get(EvaluationGroup.defaultGroup()));

        // test the form of evaluate() that should override the portfolio benchmark
        results = evaluator.evaluate(portfolio, overrideBenchmark);

        assertEquals("unexpected number of results", 3, results.size());
        assertTrue("always-pass rule should have passed",
                results.get(passRule).get(EvaluationGroup.defaultGroup()));
        assertFalse("always-fail rule should have failed",
                results.get(failRule).get(EvaluationGroup.defaultGroup()));
        assertFalse("override benchmark should have been used",
                results.get(usePortfolioBenchmarkRule).get(EvaluationGroup.defaultGroup()));

        HashSet<Rule> overrideRules = new HashSet<>();
        overrideRules.add(usePortfolioBenchmarkRule);

        // test the form of evaluate() that should override the portfolio rules
        results = evaluator.evaluate(overrideRules.stream(), portfolio);

        assertEquals("unexpected number of results", 1, results.size());
        assertTrue("portfolio benchmark should have been used",
                results.get(usePortfolioBenchmarkRule).get(EvaluationGroup.defaultGroup()));

        // test the form of evaluate() that should override the portfolio rules and benchmark
        results = evaluator.evaluate(overrideRules.stream(), portfolio, overrideBenchmark);

        assertEquals("unexpected number of results", 1, results.size());
        assertFalse("override benchmark should have been used",
                results.get(usePortfolioBenchmarkRule).get(EvaluationGroup.defaultGroup()));
    }
}
