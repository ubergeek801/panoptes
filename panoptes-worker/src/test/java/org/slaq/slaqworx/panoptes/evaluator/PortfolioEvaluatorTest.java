package org.slaq.slaqworx.panoptes.evaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.GenericRule;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.SecurityAttributeGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.TopNSecurityAttributeAggregator;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.test.TestRuleProvider;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * {@code PortfolioEvaluatorTest} tests the functionality of the {@code LocalPortfolioEvaluator} and
 * {@code ClusterPortfolioEvaluator}.
 *
 * @author jeremy
 */
@MicronautTest
public class PortfolioEvaluatorTest {
  /**
   * {@code DummyRule} facilitates testing rule evaluation behavior by always passing or always
   * failing.
   */
  private static class DummyRule extends GenericRule {
    private final boolean isPass;

    public DummyRule(String description, boolean isPass) {
      super(description);
      this.isPass = isPass;
    }

    @Override
    public ValueResult eval(PositionSupplier positions, EvaluationGroup evaluationGroup,
                            EvaluationContext evaluationContext) {
      return new ValueResult(isPass);
    }
  }

  /**
   * {@code ExceptionThrowingRule} facilitates testing rule evaluation behavior by always
   * throwing a
   * runtime exception.
   */
  private static class ExceptionThrowingRule extends GenericRule {
    public ExceptionThrowingRule(String description) {
      super(description);
    }

    @Override
    protected ValueResult eval(PositionSupplier positions, EvaluationGroup evaluationGroup,
                               EvaluationContext evaluationContext) {
      throw new RuntimeException("exception test");
    }
  }

  /**
   * {@code UseBenchmarkRule} facilitates testing whether a specific benchmark was used during a
   * rule evaluation.
   */
  private static class UseBenchmarkRule extends GenericRule {
    private final PortfolioKey benchmarkKey;

    public UseBenchmarkRule(String description, PortfolioKey benchmarkKey) {
      super(description);
      this.benchmarkKey = benchmarkKey;
    }

    @Override
    public ValueResult eval(PositionSupplier positions, EvaluationGroup evaluationGroup,
                            EvaluationContext evaluationContext) {
      return new ValueResult(benchmarkKey.equals(positions.getPortfolioKey()));
    }

    @Override
    public boolean isBenchmarkSupported() {
      return true;
    }
  }

  @Inject
  private AssetCache assetCache;
  @Inject
  private ClusterPortfolioEvaluator clusterEvaluator;

  /**
   * Tests that {@code evaluate()} behaves as expected with {@code GroupAggregator}s (also
   * implicitly tests {@code TopNSecurityAttributeAggregator}).
   *
   * @throws Exception
   *     if an unexpected error occurs
   */
  @Test
  public void testEvaluateAggregation() throws Exception {
    Security iss1Sec1 = TestUtil.createTestSecurity(assetCache, "iss1Sec1", "ISSFOO", 1);
    Security iss1Sec2 = TestUtil.createTestSecurity(assetCache, "iss1Sec2", "ISSFOO", 1);
    Security iss1Sec3 = TestUtil.createTestSecurity(assetCache, "iss1Sec3", "ISSFOO", 1);
    Security iss2Sec1 = TestUtil.createTestSecurity(assetCache, "iss2Sec1", "ISSBAR", 1);
    Security iss2Sec2 = TestUtil.createTestSecurity(assetCache, "iss2Sec2", "ISSBAR", 1);
    Security iss3Sec1 = TestUtil.createTestSecurity(assetCache, "iss3Sec1", "ISSBAZ", 1);
    Security iss4Sec1 = TestUtil.createTestSecurity(assetCache, "iss4Sec1", "ISSABC", 1);
    Security iss4Sec2 = TestUtil.createTestSecurity(assetCache, "iss4Sec2", "ISSABC", 1);
    Security iss5Sec1 = TestUtil.createTestSecurity(assetCache, "iss5Sec1", "ISSDEF", 1);
    Security iss6Sec1 = TestUtil.createTestSecurity(assetCache, "iss6Sec1", "ISSGHI", 1);

    // the top 3 issuers are ISSFOO (300 or 30%), ISSBAR (200 or 20%), ISSABC (200 or 20%) for a
    // total of 70% concentration; the top 2 are 50% concentration
    HashSet<Position> positions = new HashSet<>();
    Position iss1Sec1Pos = TestUtil.createTestPosition(assetCache, 100, iss1Sec1);
    positions.add(iss1Sec1Pos);
    Position iss1Sec2Pos = TestUtil.createTestPosition(assetCache, 100, iss1Sec2);
    positions.add(iss1Sec2Pos);
    Position iss1Sec3Pos = TestUtil.createTestPosition(assetCache, 100, iss1Sec3);
    positions.add(iss1Sec3Pos);
    Position iss2Sec1Pos = TestUtil.createTestPosition(assetCache, 100, iss2Sec1);
    positions.add(iss2Sec1Pos);
    Position iss2Sec2Pos = TestUtil.createTestPosition(assetCache, 100, iss2Sec2);
    positions.add(iss2Sec2Pos);
    Position iss3Sec1Pos = TestUtil.createTestPosition(assetCache, 100, iss3Sec1);
    positions.add(iss3Sec1Pos);
    Position iss4Sec1Pos = TestUtil.createTestPosition(assetCache, 100, iss4Sec1);
    positions.add(iss4Sec1Pos);
    Position iss4Sec2Pos = TestUtil.createTestPosition(assetCache, 100, iss4Sec2);
    positions.add(iss4Sec2Pos);
    Position iss5Sec1Pos = TestUtil.createTestPosition(assetCache, 100, iss5Sec1);
    positions.add(iss5Sec1Pos);
    Position iss6Sec1Pos = TestUtil.createTestPosition(assetCache, 100, iss6Sec1);
    positions.add(iss6Sec1Pos);

    HashMap<RuleKey, Rule> rules = new HashMap<>();
    TestRuleProvider.getInstance();
    ConcentrationRule top2issuerRule = TestRuleProvider.createTestConcentrationRule(assetCache,
        new RuleKey("top2"), "top 2 issuer concentration", null, null, 0.25,
        new TopNSecurityAttributeAggregator(SecurityAttribute.issuer, 2));
    rules.put(top2issuerRule.getKey(), top2issuerRule);
    ConcentrationRule top3issuerRule = TestRuleProvider.createTestConcentrationRule(assetCache,
        new RuleKey("top3"), "top 3 issuer concentration", null, null, 0.75,
        new TopNSecurityAttributeAggregator(SecurityAttribute.issuer, 3));
    rules.put(top3issuerRule.getKey(), top3issuerRule);
    ConcentrationRule top10issuerRule = TestRuleProvider.createTestConcentrationRule(assetCache,
        new RuleKey("top10"), "top 10 issuer concentration", null, null, 0.999,
        new TopNSecurityAttributeAggregator(SecurityAttribute.issuer, 10));
    rules.put(top10issuerRule.getKey(), top10issuerRule);

    Portfolio portfolio = TestUtil.createTestPortfolio(assetCache, "test portfolio", "test",
        positions, null, rules.values());

    LocalPortfolioEvaluator localEvaluator = new LocalPortfolioEvaluator(assetCache);
    Map<RuleKey, EvaluationResult> allResults = localEvaluator
        .evaluate(portfolio.getKey(), TestUtil.defaultTestEvaluationContext()).get();
    validateAggregationResults(allResults, rules, top2issuerRule, top3issuerRule,
        top10issuerRule);

    allResults = clusterEvaluator
        .evaluate(portfolio.getKey(), TestUtil.defaultTestEvaluationContext()).get();
    validateAggregationResults(allResults, rules, top2issuerRule, top3issuerRule,
        top10issuerRule);
  }

  /**
   * Tests that {@code evaluate()} behaves as expected when an unexpected exception is thrown
   * (expect the unexpected!).
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

    Position dummyPosition = new SimplePosition(1, TestUtil.s1.getKey());
    Set<Position> dummyPositions = Set.of(dummyPosition);

    Map<RuleKey, EvaluationResult> results =
        new LocalPortfolioEvaluator(assetCache).evaluate(rules.values().stream(),
            new Portfolio(new PortfolioKey("testPortfolio", 1), "test", dummyPositions),
            null, TestUtil.defaultTestEvaluationContext());
    // 3 distinct rules should result in 3 evaluations
    assertEquals(3, results.size(), "unexpected number of results");
    assertTrue(
        results.get(passRule.getKey()).getResult(EvaluationGroup.defaultGroup()).isPassed(),
        "always-pass rule should have passed");
    assertFalse(
        results.get(failRule.getKey()).getResult(EvaluationGroup.defaultGroup()).isPassed(),
        "always-fail rule should have failed");
    assertFalse(results.get(exceptionRule.getKey()).getResult(EvaluationGroup.defaultGroup())
        .isPassed(), "exception-throwing rule should have failed");
  }

  /**
   * Tests that {@code evaluate()} behaves as expected when input {@code Position}s are
   * partitioned
   * among multiple {@code EvaluationGroup}s. This also implicitly tests {@code ConcentrationRule}
   * and {@code WeightedAverageRule}.
   *
   * @throws Exception
   *     if an unexpected error occurs
   */
  @Test
  public void testEvaluateGroups() throws Exception {
    LocalPortfolioEvaluator evaluator = new LocalPortfolioEvaluator(assetCache);

    Map<SecurityAttribute<?>,
        ? super Object> usdAttributes = SecurityAttribute.mapOf(SecurityAttribute.currency,
        "USD", SecurityAttribute.rating1Value, 90d, SecurityAttribute.duration, 3d,
        SecurityAttribute.issuer, "ISSFOO", SecurityAttribute.price, 1d);
    Security usdSecurity = TestUtil.createTestSecurity(assetCache, "usd", usdAttributes);

    Map<SecurityAttribute<?>,
        ? super Object> nzdAttributes = SecurityAttribute.mapOf(SecurityAttribute.currency,
        "NZD", SecurityAttribute.rating1Value, 80d, SecurityAttribute.duration, 4d,
        SecurityAttribute.issuer, "ISSFOO", SecurityAttribute.price, 1d);
    Security nzdSecurity = TestUtil.createTestSecurity(assetCache, "nzd", nzdAttributes);

    Map<SecurityAttribute<?>,
        ? super Object> cadAttributes = SecurityAttribute.mapOf(SecurityAttribute.currency,
        "CAD", SecurityAttribute.rating1Value, 75d, SecurityAttribute.duration, 5d,
        SecurityAttribute.issuer, "ISSBAR", SecurityAttribute.price, 1d);
    Security cadSecurity = TestUtil.createTestSecurity(assetCache, "cad", cadAttributes);

    HashSet<Position> positions = new HashSet<>();
    // value = 100, weighted rating = 9_000, weighted duration = 300
    Position usdPosition1 = TestUtil.createTestPosition(assetCache, 100, usdSecurity.getKey());
    positions.add(usdPosition1);
    // value = 200, weighted rating = 18_000, weighted duration = 600
    Position usdPosition2 = TestUtil.createTestPosition(assetCache, 200, usdSecurity.getKey());
    positions.add(usdPosition2);
    // value = 300, weighted rating = 24_000, weighted duration = 1_200
    Position nzdPosition1 = TestUtil.createTestPosition(assetCache, 300, nzdSecurity.getKey());
    positions.add(nzdPosition1);
    // value = 400, weighted rating = 32_000, weighted duration = 1_600
    Position nzdPosition2 = TestUtil.createTestPosition(assetCache, 400, nzdSecurity.getKey());
    positions.add(nzdPosition2);
    // value = 500, weighted rating = 37_500, weighted duration = 2_500
    Position cadPosition1 = TestUtil.createTestPosition(assetCache, 500, cadSecurity.getKey());
    positions.add(cadPosition1);
    // value = 600, weighted rating = 45_000, weighted duration = 3_000
    Position cadPosition2 = TestUtil.createTestPosition(assetCache, 600, cadSecurity.getKey());
    positions.add(cadPosition2);

    HashMap<RuleKey, ConfigurableRule> rules = new HashMap<>();
    ConfigurableRule durationRule = TestRuleProvider.createTestWeightedAverageRule(assetCache,
        null, "currency-grouped duration rule", null, SecurityAttribute.duration, null, 4d,
        new SecurityAttributeGroupClassifier(SecurityAttribute.currency));
    rules.put(durationRule.getKey(), durationRule);
    ConfigurableRule qualityRule = TestRuleProvider.createTestWeightedAverageRule(assetCache,
        null, "ungrouped quality rule", null, SecurityAttribute.rating1Value, 80d, null,
        null);
    rules.put(qualityRule.getKey(), qualityRule);
    ConfigurableRule issuerRule = TestRuleProvider.createTestConcentrationRule(assetCache, null,
        "issuer-grouped concentration rule", null, null, 0.5,
        new SecurityAttributeGroupClassifier(SecurityAttribute.issuer));
    rules.put(issuerRule.getKey(), issuerRule);

    // total value = 2_100, weighted rating = 165_500, weighted duration = 9_200,
    // weighted average rating = 78.80952381, weighted average duration = 4.380952381
    Portfolio portfolio = TestUtil.createTestPortfolio(assetCache, "test", "test", positions,
        null, rules.values());

    Map<RuleKey, EvaluationResult> results =
        evaluator.evaluate(portfolio.getKey(), new EvaluationContext()).get();

    // all rules should have entries
    assertEquals(rules.size(), results.size(),
        "number of evaluated rules should match number of portfolio rules");

    // each Rule's entry should have a number of results equal to the number of distinct groups
    // for that Rule
    EvaluationResult durationResults = results.get(durationRule.getKey());
    assertEquals(3, durationResults.size(),
        "number of duration rule results should match number of currencies");
    EvaluationResult qualityResults = results.get(qualityRule.getKey());
    assertEquals(1, qualityResults.size(), "quality rule results should have a single group");
    EvaluationResult issuerResults = results.get(issuerRule.getKey());
    assertEquals(2, issuerResults.size(),
        "number of issuer rule results should match number of issuers");

    // the duration rule is grouped by currency, so we should find results for USD, NZD, CAD;
    // USD duration = (300 + 600) / (100 + 200) = 3 which should pass
    assertTrue(durationResults.getResult(new EvaluationGroup("USD", null)).isPassed(),
        "duration rule should have passed for USD");
    // NZD duration = (1_200 + 1_600) / (300 + 400) = 4 which should pass
    assertTrue(durationResults.getResult(new EvaluationGroup("NZD", null)).isPassed(),
        "duration rule should have passed for NZD");
    // CAD duration = (2_500 + 3_000) / (500 + 600) = 5 which should fail
    assertFalse(durationResults.getResult(new EvaluationGroup("CAD", null)).isPassed(),
        "duration rule should have failed for CAD");

    // the quality rule is not grouped, so should have a single result for the default group
    assertFalse(qualityResults.getResult(EvaluationGroup.defaultGroup()).isPassed(),
        "quality rule should have failed");

    // the issuer rule is grouped by issuer, so we should find results for ISSFOO, ISSBAR;
    // ISSFOO concentration = (100 + 200 + 300 + 400) / 2_100 = 0.476190476 which should pass
    assertTrue(issuerResults.getResult(new EvaluationGroup("ISSFOO", null)).isPassed(),
        "issuer rule should have passed for ISSFOO");
    // ISSBAR concentration = (500 + 600) / 2_100 = 0.523809524 which should fail
    assertFalse(issuerResults.getResult(new EvaluationGroup("ISSBAR", null)).isPassed(),
        "issuer rule should have failed for ISSBAR");
  }

  /**
   * Tests that {@code evaluate()} behaves as expected when overrides are applied.
   *
   * @throws Exception
   *     if an unexpected error occurs
   */
  @Test
  public void testEvaluateOverrides() throws Exception {
    Position dummyPosition = new SimplePosition(1, TestUtil.s1.getKey());
    Set<Position> dummyPositions = Set.of(dummyPosition);

    Position overridePosition = new SimplePosition(1, TestUtil.s2.getKey());
    Set<Position> overridePositions = Set.of(overridePosition);

    Portfolio portfolioBenchmark = new Portfolio(new PortfolioKey("testPortfolioBenchmark", 1),
        "test", dummyPositions);
    Portfolio overrideBenchmark = new Portfolio(new PortfolioKey("testOverrideBenchmark", 1),
        "test", overridePositions);

    DummyRule passRule = new DummyRule("testPass", true);
    DummyRule failRule = new DummyRule("testFail", false);
    UseBenchmarkRule usePortfolioBenchmarkRule =
        new UseBenchmarkRule("testBenchmarkId", portfolioBenchmark.getKey());

    Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", dummyPositions,
        portfolioBenchmark.getKey(),
        List.of(passRule, failRule, usePortfolioBenchmarkRule));
    // a really dumb PortfolioProvider that returns either portfolioBenchmark,
    // overrideBenchmark or portfolio
    PortfolioProvider portfolioProvider =
        (k -> (portfolioBenchmark.getKey().equals(k) ? portfolioBenchmark
            : (overrideBenchmark.getKey().equals(k) ? overrideBenchmark : portfolio)));

    LocalPortfolioEvaluator evaluator = new LocalPortfolioEvaluator(portfolioProvider);

    // test the form of evaluate() that should use the portfolio defaults
    Map<RuleKey,
        EvaluationResult> results = evaluator.evaluate(portfolio.getKey(),
        new EvaluationContext(TestUtil.testSecurityProvider(), portfolioProvider))
        .get();

    // 3 distinct rules should result in 3 evaluations
    assertEquals(3, results.size(), "unexpected number of results");
    assertTrue(
        results.get(passRule.getKey()).getResult(EvaluationGroup.defaultGroup()).isPassed(),
        "always-pass rule should have passed");
    assertFalse(
        results.get(failRule.getKey()).getResult(EvaluationGroup.defaultGroup()).isPassed(),
        "always-fail rule should have failed");
    assertTrue(
        results.get(usePortfolioBenchmarkRule.getKey())
            .getResult(EvaluationGroup.defaultGroup()).isPassed(),
        "portfolio benchmark should have been used");

    HashSet<Rule> overrideRules = new HashSet<>();
    overrideRules.add(usePortfolioBenchmarkRule);

    // test the form of evaluate() that should override the Portfolio rules
    results = evaluator.evaluate(overrideRules.stream(), portfolio, null,
        TestUtil.defaultTestEvaluationContext());

    assertEquals(1, results.size(), "unexpected number of results");
    assertTrue(
        results.get(usePortfolioBenchmarkRule.getKey())
            .getResult(EvaluationGroup.defaultGroup()).isPassed(),
        "portfolio benchmark should have been used");
  }

  /**
   * Validates that the aggregation rule results produced in {@code testEvaluateAggregation()} are
   * as expected.
   *
   * @param allResults
   *     the results of evaluation
   * @param rules
   *     the {@code Rule}s created for the evaluated {@code Portfolio}
   * @param top2issuerRule
   *     the {@code Rule} corresponding to the top 2 issuer test
   * @param top3issuerRule
   *     the {@code Rule} corresponding to the top 3 issuer test
   * @param top10issuerRule
   *     the {@code Rule} corresponding to the top 10 issuer test
   */
  protected void validateAggregationResults(Map<RuleKey, EvaluationResult> allResults,
                                            Map<RuleKey, Rule> rules, Rule top2issuerRule,
                                            Rule top3issuerRule,
                                            Rule top10issuerRule) {
    assertEquals(rules.size(), allResults.size(),
        "number of results should equal number of Rules");

    EvaluationResult top2IssuerResults = allResults.get(top2issuerRule.getKey());
    EvaluationResult top3IssuerResults = allResults.get(top3issuerRule.getKey());
    EvaluationResult top10IssuerResults = allResults.get(top10issuerRule.getKey());

    assertNotNull(top2IssuerResults, "should have found results for top 2 issuer rule");
    assertNotNull(top3IssuerResults, "should have found results for top 3 issuer rule");
    assertNotNull(top10IssuerResults, "should have found results for top 10 issuer rule");

    // all Rules should create one group for each top-N issuer (capped at 6 which is the number
    // of distinct issuers in the test) plus one for the aggregate
    assertEquals(3, top2IssuerResults.size(),
        "unexpected number of groups for top 2 issuer rule");
    assertEquals(4, top3IssuerResults.size(),
        "unexpected number of groups for top 3 issuer rule");
    assertEquals(7, top10IssuerResults.size(),
        "unexpected number of groups for top 10 issuer rule");

    top2IssuerResults.getResults().forEach((group, result) -> {
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
              "top 2 issuer rule should have failed for aggregate group "
                  + group.getId());
      }
    });

    top3IssuerResults.getResults().forEach((group, result) -> {
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
          assertTrue(result.isPassed(),
              "top 3 issuer rule should have passed for aggregate group "
                  + group.getId());
      }
    });

    top10IssuerResults.getResults().forEach((group, result) -> {
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
              "top 10 issuer rule should have failed for aggregate group "
                  + group.getId());
      }
    });
  }
}
