package org.slaq.slaqworx.panoptes.evaluator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.LimitRule;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;
import org.slaq.slaqworx.panoptes.test.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * Tests the functionality of the {@link BenchmarkComparator}.
 *
 * @author jeremy
 */
public class BenchmarkComparatorTest {
  private final TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

  /**
   * Tests that benchmark-relative {@link ConcentrationRule} comparison behaves as expected.
   */
  @Test
  public void testCompare_benchmarkRelativeConcentration() {
    EvaluationContext context = TestUtil.defaultTestEvaluationContext();

    // the Rule tests that the concentration of currency = BRL is between 95% and 105% of the
    // benchmark
    ConcentrationRule rule = new ConcentrationRule(null, "test rule",
        c -> "BRL".equals(c.getPosition().getAttributeValue(SecurityAttribute.currency, context)),
        0.95, 1.05, null);

    Security brlSecurity = securityProvider.newSecurity("brl",
        SecurityAttribute.mapOf(SecurityAttribute.currency, "BRL", SecurityAttribute.price, 1d));
    Security nzdSecurity = securityProvider.newSecurity("nzd",
        SecurityAttribute.mapOf(SecurityAttribute.currency, "NZD", SecurityAttribute.price, 1d));

    // create a benchmark with 50% concentration in BRL
    HashSet<Position> benchmarkPositions = new HashSet<>();
    benchmarkPositions.add(new SimplePosition(100, brlSecurity.getKey()));
    benchmarkPositions.add(new SimplePosition(100, nzdSecurity.getKey()));
    Portfolio benchmark1 =
        new Portfolio(new PortfolioKey("testBenchmark", 1), "test", benchmarkPositions);

    // create a Portfolio with 56% concentration in BRL
    HashSet<Position> positions = new HashSet<>();
    positions.add(new SimplePosition(56, brlSecurity.getKey()));
    positions.add(new SimplePosition(44, nzdSecurity.getKey()));
    Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

    EvaluationResult baseResult = new RuleEvaluator(rule, portfolio, context).call();
    EvaluationResult benchmarkResult = new RuleEvaluator(rule, benchmark1, context).call();
    EvaluationResult finalResult =
        new BenchmarkComparator().compare(baseResult, benchmarkResult, rule);
    assertFalse(finalResult.isPassed(), "portfolio with == 56% concentration should have failed");

    // create a Portfolio with 44% concentration in BRL
    positions = new HashSet<>();
    positions.add(new SimplePosition(44, brlSecurity.getKey()));
    positions.add(new SimplePosition(56, nzdSecurity.getKey()));
    portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

    baseResult = new RuleEvaluator(rule, portfolio, context).call();
    benchmarkResult = new RuleEvaluator(rule, benchmark1, context).call();
    finalResult = new BenchmarkComparator().compare(baseResult, benchmarkResult, rule);

    assertFalse(finalResult.isPassed(), "portfolio with == 44% concentration should have failed");

    // create a Portfolio with 52.5% (50% * 105%) concentration in BRL
    positions = new HashSet<>();
    positions.add(new SimplePosition(52.5, brlSecurity.getKey()));
    positions.add(new SimplePosition(47.5, nzdSecurity.getKey()));
    portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

    baseResult = new RuleEvaluator(rule, portfolio, context).call();
    benchmarkResult = new RuleEvaluator(rule, benchmark1, context).call();
    finalResult = new BenchmarkComparator().compare(baseResult, benchmarkResult, rule);

    assertTrue(finalResult.isPassed(), "portfolio with == 52.5% concentration should have passed");

    // create a Portfolio with 47.5% (50% * 95%) concentration in BRL
    positions = new HashSet<>();
    positions.add(new SimplePosition(47.5, brlSecurity.getKey()));
    positions.add(new SimplePosition(52.5, nzdSecurity.getKey()));
    portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

    baseResult = new RuleEvaluator(rule, portfolio, context).call();
    benchmarkResult = new RuleEvaluator(rule, benchmark1, context).call();
    finalResult = new BenchmarkComparator().compare(baseResult, benchmarkResult, rule);

    assertTrue(finalResult.isPassed(), "portfolio with == 52.5% concentration should have passed");

    // create a benchmark with 0% concentration in BRL
    benchmarkPositions = new HashSet<>();
    benchmarkPositions.add(new SimplePosition(100, nzdSecurity.getKey()));
    Portfolio benchmark2 =
        new Portfolio(new PortfolioKey("testBenchmark", 1), "test", benchmarkPositions);

    baseResult = new RuleEvaluator(rule, portfolio, context).call();
    benchmarkResult = new RuleEvaluator(rule, benchmark2, context).call();
    finalResult = new BenchmarkComparator().compare(baseResult, benchmarkResult, rule);

    // any concentration is infinitely higher than the benchmark, so should fail
    assertFalse(finalResult.isPassed(), "portfolio with any concentration should have failed");

    // the Rule tests that the concentration of currency = BRL is at least 95% of the
    // benchmark
    rule = new ConcentrationRule(null, "test rule",
        c -> "BRL".equals(c.getPosition().getAttributeValue(SecurityAttribute.currency, context)),
        0.95, null, null);

    // create a portfolio with 0% concentration in BRL
    positions = new HashSet<>();
    positions.add(new SimplePosition(100, nzdSecurity.getKey()));
    portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

    baseResult = new RuleEvaluator(rule, portfolio, context).call();
    benchmarkResult = new RuleEvaluator(rule, benchmark2, context).call();
    finalResult = new BenchmarkComparator().compare(baseResult, benchmarkResult, rule);

    // zero concentration is at least zero, so should pass
    assertTrue(finalResult.isPassed(), "portfolio with any concentration should have passed");

    // create a Portfolio with 1% concentration in BRL
    positions = new HashSet<>();
    positions.add(new SimplePosition(1, brlSecurity.getKey()));
    positions.add(new SimplePosition(99, nzdSecurity.getKey()));
    portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

    baseResult = new RuleEvaluator(rule, portfolio, context).call();
    benchmarkResult = new RuleEvaluator(rule, benchmark2, context).call();
    finalResult = new BenchmarkComparator().compare(baseResult, benchmarkResult, rule);

    // any concentration is at least zero, so should pass
    assertTrue(finalResult.isPassed(),
        "portfolio with any concentration should have passed: " + finalResult);

    // the Rule tests that the concentration of currency = BRL is at most 105% of the
    // benchmark
    rule = new ConcentrationRule(null, "test rule",
        c -> "BRL".equals(c.getPosition().getAttributeValue(SecurityAttribute.currency, context)),
        null, 1.05, null);

    baseResult = new RuleEvaluator(rule, portfolio, context).call();
    benchmarkResult = new RuleEvaluator(rule, benchmark2, context).call();
    finalResult = new BenchmarkComparator().compare(baseResult, benchmarkResult, rule);

    // any concentration is infinitely higher than the benchmark, so should fail
    assertFalse(finalResult.isPassed(), "portfolio with any concentration should have failed");
  }

  /**
   * Tests that comparison behaves as expected with a benchmark-relative {@link
   * WeightedAverageRule}.
   */
  @Test
  public void testCompare_benchmarkRelativeWeightedAverage() {
    EvaluationContext context = TestUtil.defaultTestEvaluationContext();

    // include only Positions that are not in the TestUtil.s2 Security
    Predicate<PositionEvaluationContext> filter =
        (c -> !c.getPosition().getSecurityKey().equals(TestUtil.s2.getKey()));

    // Once filtered, p1's sole Position will be 1000 of s1, which has weighted moovyRating
    // (1000 * 90) / 1000 = 90. Meanwhile, p3's Positions will be 500 of s1 (500 * 90) and 200
    // of s3 (200 * 80), for a weighted moovyRating of (45_000 + 16_000) / 700 = 87.142857143.
    // Thus p1's weighted average is 1.032786885 * p3's.

    LimitRule rule =
        new WeightedAverageRule<>(null, "test", filter, TestUtil.moovyRating, 1.035, null, null);
    EvaluationResult baseResult = new RuleEvaluator(rule, TestUtil.p1, context).call();
    EvaluationResult benchmarkResult = new RuleEvaluator(rule, TestUtil.p3, context).call();
    EvaluationResult finalResult =
        new BenchmarkComparator().compare(baseResult, benchmarkResult, rule);
    assertFalse(finalResult.isPassed(), "rule with 103.5% lower limit should have failed");

    rule = new WeightedAverageRule<>(null, "test", filter, TestUtil.moovyRating, 1.03, null, null);
    baseResult = new RuleEvaluator(rule, TestUtil.p1, context).call();
    benchmarkResult = new RuleEvaluator(rule, TestUtil.p3, context).call();
    finalResult = new BenchmarkComparator().compare(baseResult, benchmarkResult, rule);
    assertTrue(finalResult.isPassed(), "rule with 103% lower limit should have passed");

    // p1's concentration in non-s2 is 1000 / 1500 = 0.666...; p3's concentration is 700 / 1700
    // = 0.411764706; p1 is thus 1.619047619 of the benchmark and should thus fail this rule
    rule = new ConcentrationRule(null, "test rule", filter, 1.62, null, null);
    baseResult = new RuleEvaluator(rule, TestUtil.p1, context).call();
    benchmarkResult = new RuleEvaluator(rule, TestUtil.p3, context).call();
    finalResult = new BenchmarkComparator().compare(baseResult, benchmarkResult, rule);
    assertFalse(finalResult.isPassed(), "rule with 162% lower limit should have failed");
    // ...and should pass this rule
    rule = new ConcentrationRule(null, "test rule", filter, 1.619, null, null);
    baseResult = new RuleEvaluator(rule, TestUtil.p1, context).call();
    benchmarkResult = new RuleEvaluator(rule, TestUtil.p3, context).call();
    finalResult = new BenchmarkComparator().compare(baseResult, benchmarkResult, rule);
    assertTrue(finalResult.isPassed(), "rule with 161.9% lower limit should have passed");
  }
}
