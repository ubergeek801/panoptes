package org.slaq.slaqworx.panoptes.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;
import org.slaq.slaqworx.panoptes.asset.Portfolio;

/**
 * RuleEvaluatorTest tests the functionality of the RuleEvaluator.
 *
 * @author jeremy
 */
public class RuleEvaluatorTest {
    private static class BenchmarkIdRule extends Rule {
        private final Portfolio benchmark;

        public BenchmarkIdRule(String description, Portfolio benchmark) {
            super(description);
            this.benchmark = benchmark;
        }

        @Override
        public boolean evaluate(Portfolio portfolio, Portfolio benchmark) {
            return this.benchmark.equals(benchmark);
        }

        @Override
        protected double eval(Portfolio portfolio, Portfolio benchmark) {
            // value is not used
            return 0;
        }
    }

    private static class DummyRule extends Rule {
        private final boolean isPass;

        public DummyRule(String description, boolean isPass) {
            super(description);
            this.isPass = isPass;
        }

        @Override
        public boolean evaluate(Portfolio portfolio, Portfolio benchmark) {
            return isPass;
        }

        @Override
        protected double eval(Portfolio portfolio, Portfolio benchmark) {
            // value is not used
            return 0;
        }
    }

    private static class ExceptionThrowingRule extends Rule {
        public ExceptionThrowingRule(String description) {
            super(description);
        }

        @Override
        protected double eval(Portfolio portfolio, Portfolio benchmark) {
            throw new RuntimeException("exception test");
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

        // the dummy rules don't even look at Positions so we should be able to get away with none
        Map<Rule, Boolean> results = new RuleEvaluator().evaluate(rules.stream(),
                new Portfolio("testPortfolio", Collections.emptySet()));
        // 3 distinct rules should result in 3 evaluations
        assertEquals("unexpected number of results", 3, results.size());
        assertTrue("always-pass rule should have passed", results.get(passRule));
        assertFalse("always-fail rule should have failed", results.get(failRule));
        assertFalse("exception-throwing rule should have failed", results.get(exceptionRule));
    }

    /**
     * Tests that evaluate() behaves as expected when overrides are applied.
     */
    @Test
    public void testEvaluateOverrides() {
        RuleEvaluator evaluator = new RuleEvaluator();

        // the dummy rules don't even look at Positions so we should be able to get away with none
        Portfolio portfolioBenchmark =
                new Portfolio("testPortfolioBenchmark", Collections.emptySet());
        Portfolio overrideBenchmark =
                new Portfolio("testOverrideBenchmark", Collections.emptySet());

        DummyRule passRule = new DummyRule("testPass", true);
        DummyRule failRule = new DummyRule("testFail", false);
        BenchmarkIdRule usePortfolioBenchmarkRule =
                new BenchmarkIdRule("testBenchmarkId", portfolioBenchmark);

        HashSet<Rule> portfolioRules = new HashSet<>();
        portfolioRules.add(passRule);
        portfolioRules.add(failRule);
        portfolioRules.add(usePortfolioBenchmarkRule);

        Portfolio portfolio =
                new Portfolio("test", Collections.emptySet(), portfolioBenchmark, portfolioRules);

        // test the form of evaluate() that should use the portfolio defaults
        Map<Rule, Boolean> results = evaluator.evaluate(portfolio);

        // 3 distinct rules should result in 3 evaluations
        assertEquals("unexpected number of results", 3, results.size());
        assertTrue("always-pass rule should have passed", results.get(passRule));
        assertFalse("always-fail rule should have failed", results.get(failRule));
        assertTrue("portfolio benchmark should have been used",
                results.get(usePortfolioBenchmarkRule));

        // test the form of evaluate() that should override the portfolio benchmark
        results = evaluator.evaluate(portfolio, overrideBenchmark);

        assertEquals("unexpected number of results", 3, results.size());
        assertTrue("always-pass rule should have passed", results.get(passRule));
        assertFalse("always-fail rule should have failed", results.get(failRule));
        assertFalse("override benchmark should have been used",
                results.get(usePortfolioBenchmarkRule));

        HashSet<Rule> overrideRules = new HashSet<>();
        overrideRules.add(usePortfolioBenchmarkRule);

        // test the form of evaluate() that should override the portfolio rules
        results = evaluator.evaluate(overrideRules.stream(), portfolio);

        assertEquals("unexpected number of results", 1, results.size());
        assertTrue("portfolio benchmark should have been used",
                results.get(usePortfolioBenchmarkRule));

        // test the form of evaluate() that should override the portfolio rules and benchmark
        results = evaluator.evaluate(overrideRules.stream(), portfolio, overrideBenchmark);

        assertEquals("unexpected number of results", 1, results.size());
        assertFalse("override benchmark should have been used",
                results.get(usePortfolioBenchmarkRule));
    }
}
