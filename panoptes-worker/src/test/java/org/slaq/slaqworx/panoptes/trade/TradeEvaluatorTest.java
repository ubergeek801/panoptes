package org.slaq.slaqworx.panoptes.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.calc.WeightedAveragePositionCalculator;
import org.slaq.slaqworx.panoptes.evaluator.ClusterPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.evaluator.LocalPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.GroovyPositionFilter;
import org.slaq.slaqworx.panoptes.rule.MarketValueRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Impact;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;
import org.slaq.slaqworx.panoptes.test.TestRuleProvider;
import org.slaq.slaqworx.panoptes.test.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * Tests the functionality of the {@link TradeEvaluator} implementations, primarily {@link
 * LocalTradeEvaluator}.
 *
 * @author jeremy
 */
@MicronautTest
public class TradeEvaluatorTest {
  @Inject
  private AssetCache assetCache;
  @Inject
  private ClusterPortfolioEvaluator clusterEvaluator;

  /**
   * Tests that {@code evaluate()} behaves as expected.
   *
   * @throws Exception
   *     if an unexpected error occurs
   */
  @Test
  public void testEvaluate() throws Exception {
    TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

    Security s1 = securityProvider.newSecurity("TradeEvaluatorTestSec1",
        SecurityAttribute.mapOf(SecurityAttribute.duration, 3.0, SecurityAttribute.price, 100d));
    Security s2 = securityProvider.newSecurity("TradeEvaluatorTestSec2",
        SecurityAttribute.mapOf(SecurityAttribute.duration, 4.0, SecurityAttribute.price, 100d));

    HashSet<Position> p1Positions = new HashSet<>();
    p1Positions.add(new SimplePosition(1_000, s1.getKey()));

    // to keep things simple, all Rules test against duration, with some conflicting
    // assertions
    HashMap<RuleKey, ConfigurableRule> p1Rules = new HashMap<>();
    // since the Portfolio is already within (at) the limit, and the Trade pushes it out, this
    // Rule should fail
    ConfigurableRule p1Rule1 =
        new WeightedAverageRule<>(new RuleKey("p1Rule1"), "WeightedAverage<=3.0", null,
            SecurityAttribute.duration, null, 3d, null);
    p1Rules.put(p1Rule1.getKey(), p1Rule1);
    // since the Portfolio is already above the limit, and the Trade makes it worse, this Rule
    // should fail
    ConfigurableRule p1Rule2 =
        new WeightedAverageRule<>(new RuleKey("p1Rule2"), "WeightedAverage<=2.0", null,
            SecurityAttribute.duration, null, 2d, null);
    p1Rules.put(p1Rule2.getKey(), p1Rule2);
    // since the Portfolio is already below the limit, and the Trade improves it, this Rule
    // should pass
    ConfigurableRule p1Rule3 =
        new WeightedAverageRule<>(new RuleKey("p1Rule3"), "WeightedAverage>=4.0", null,
            SecurityAttribute.duration, 4d, null, null);
    p1Rules.put(p1Rule3.getKey(), p1Rule3);
    // since the Portfolio is already within the limit, and remains so with the Trade, this Rule
    // should pass
    ConfigurableRule p1Rule4 =
        new WeightedAverageRule<>(new RuleKey("p1Rule4"), "WeightedAverage<=4.0", null,
            SecurityAttribute.duration, null, 4d, null);
    p1Rules.put(p1Rule4.getKey(), p1Rule4);

    Portfolio p1 = TestUtil.testPortfolioProvider()
        .newPortfolio("TradeEvaluatorTestP1", "test", p1Positions, null, p1Rules.values());

    TaxLot t1Alloc1 = new TaxLot(1_000, s2.getKey());
    List<TaxLot> t1Allocations = List.of(t1Alloc1);
    Transaction t1 = new Transaction(p1.getKey(), t1Allocations);
    Map<PortfolioKey, Transaction> transactions = Map.of(t1.getPortfolioKey(), t1);

    LocalDate tradeDate = LocalDate.now();
    Trade trade = new Trade(tradeDate, tradeDate, transactions);
    TradeEvaluator evaluator =
        new LocalTradeEvaluator(new LocalPortfolioEvaluator(TestUtil.testPortfolioProvider()),
            TestUtil.testPortfolioProvider(), TestUtil.testSecurityProvider());
    TradeEvaluationResult result =
        evaluator.evaluate(trade, TestUtil.defaultTestEvaluationContext()).get();

    Map<EvaluationGroup, Impact> p1r1Impact =
        result.getImpacts().get(new PortfolioRuleKey(p1.getKey(), p1Rule1.getKey()));
    assertNotNull(p1r1Impact, "should have found impact for p1Rule1");
    assertEquals(Impact.NEGATIVE, p1r1Impact.get(EvaluationGroup.defaultGroup()),
        "p1Rule1 should have failed");

    Map<EvaluationGroup, Impact> p1r2Impact =
        result.getImpacts().get(new PortfolioRuleKey(p1.getKey(), p1Rule2.getKey()));
    assertNotNull(p1r2Impact, "should have found impact for p1Rule2");
    assertEquals(Impact.NEGATIVE, p1r2Impact.get(EvaluationGroup.defaultGroup()),
        "p1Rule2 should have failed");

    Map<EvaluationGroup, Impact> p1r3Impact =
        result.getImpacts().get(new PortfolioRuleKey(p1.getKey(), p1Rule3.getKey()));
    assertNotNull(p1r3Impact, "should have found impact for p1Rule3");
    assertEquals(Impact.POSITIVE, p1r3Impact.get(EvaluationGroup.defaultGroup()),
        "p1Rule3 should have passed");

    Map<EvaluationGroup, Impact> p1r4Impact =
        result.getImpacts().get(new PortfolioRuleKey(p1.getKey(), p1Rule4.getKey()));
    assertNotNull(p1r4Impact, "should have found impact for p1Rule4");
    assertEquals(Impact.NEUTRAL, p1r4Impact.get(EvaluationGroup.defaultGroup()),
        "p1Rule4 should have passed");

    // in a short-circuit evaluation we won't know which rule would have failed, or what other
    // results might be included, but the overall result should be non-compliance

    result = evaluator.evaluate(trade, TestUtil.defaultTestEvaluationContext()).get();
    assertFalse(result.isCompliant(),
        "short-circuit evaluation should have yielded non-compliance");
  }

  /**
   * Tests that {@code evaluate()} behaves as expected when using a {@link MarketValueRule} to
   * evaluate {@link Transaction} eligibility.
   *
   * @throws Exception
   *     if an unexpected error occurs
   */
  @Test
  public void testEvaluate_eligibility() throws Exception {
    Security sec1 = TestUtil.createTestSecurity(assetCache, "sec1", "nobody", 100);

    // create an eligibility Rule excluding sec1
    GroovyPositionFilter sec1Filter = GroovyPositionFilter.of("s.isin == 'sec1'");
    ArrayList<ConfigurableRule> rules = new ArrayList<>();
    MarketValueRule rule = new MarketValueRule(null, "no sec1", sec1Filter, null, 0d);
    rules.add(rule);

    // create a Portfolio with no initial Positions
    HashSet<Position> portfolioPositions = new HashSet<>();
    Portfolio portfolio = TestUtil.testPortfolioProvider()
        .newPortfolio("TradeEvaluatorTestPortfolio", "test", portfolioPositions, null, rules);

    // create a Transaction attempting to buy sec1
    HashSet<TaxLot> buyAllocations = new HashSet<>();
    TaxLot buyAlloc1 = TestUtil.createTestTaxLot(100, sec1);
    buyAllocations.add(buyAlloc1);
    Transaction buyTransaction = new Transaction(portfolio.getKey(), buyAllocations);
    LocalDate tradeDate = LocalDate.now();
    Trade buyTrade = new Trade(tradeDate, tradeDate, Map.of(portfolio.getKey(), buyTransaction));

    TradeEvaluator evaluator =
        new LocalTradeEvaluator(new LocalPortfolioEvaluator(TestUtil.testPortfolioProvider()),
            TestUtil.testPortfolioProvider(), TestUtil.testSecurityProvider());
    TradeEvaluationResult result =
        evaluator.evaluate(buyTrade, TestUtil.defaultTestEvaluationContext()).get();
    assertFalse(result.isCompliant(), "attempt to buy restricted Security should have failed");

    // create a Transaction attempting to sell sec1 (never mind that the Portfolio doesn't hold
    // it)
    HashSet<TaxLot> sellAllocations = new HashSet<>();
    TaxLot sellAlloc1 = TestUtil.createTestTaxLot(-100, sec1);
    sellAllocations.add(sellAlloc1);
    Transaction sellTransaction = new Transaction(portfolio.getKey(), sellAllocations);
    Trade sellTrade = new Trade(tradeDate, tradeDate, Map.of(portfolio.getKey(), sellTransaction));

    result = evaluator.evaluate(sellTrade, TestUtil.defaultTestEvaluationContext()).get();
    assertTrue(result.isCompliant(), "attempt to sell restricted Security should have passed");

    // create a Portfolio with an initial Position in sec1
    portfolioPositions = new HashSet<>();
    portfolioPositions.add(TestUtil.createTestPosition(assetCache, 1000d, sec1));
    portfolio = TestUtil.testPortfolioProvider()
        .newPortfolio("TradeEvaluatorTestPortfolio", "test", portfolioPositions, null, rules);

    // try to buy again
    result = evaluator.evaluate(buyTrade, TestUtil.defaultTestEvaluationContext()).get();
    assertFalse(result.isCompliant(), "attempt to buy restricted Security should have failed");

    // try to sell again
    result = evaluator.evaluate(sellTrade, TestUtil.defaultTestEvaluationContext()).get();
    assertTrue(result.isCompliant(), "attempt to sell restricted Security should have passed");
  }

  /**
   * Tests that {@code evaluateRoom()} behaves as expected.
   *
   * @throws Exception
   *     if an unexpected error occurs
   */
  @Test
  public void testEvaluateRoom() throws Exception {
    Map<SecurityAttribute<?>, ? super Object> security1Attributes =
        SecurityAttribute.mapOf(SecurityAttribute.duration, 3d, SecurityAttribute.price, 1d);
    Security security1 =
        TestUtil.testSecurityProvider().newSecurity("TradeEvaluatorTestSec3", security1Attributes);

    Position position1 = TestUtil.testPositionProvider().newPosition(null, 1_000_000, security1);
    Set<Position> p1Positions = Set.of(position1);

    WeightedAverageRule<Double> rule1 =
        new WeightedAverageRule<>(null, "weighted average: duration <= 3.5", null,
            SecurityAttribute.duration, null, 3.5, null);
    Map<RuleKey, ? extends ConfigurableRule> p1Rules = Map.of(rule1.getKey(), rule1);

    Map<SecurityAttribute<?>, ? super Object> security2Attributes =
        SecurityAttribute.mapOf(SecurityAttribute.duration, 4d, SecurityAttribute.price, 1d);
    Security trialSecurity =
        TestUtil.testSecurityProvider().newSecurity("TradeEvaluatorTestSec4", security2Attributes);

    Portfolio portfolio = TestUtil.testPortfolioProvider()
        .newPortfolio("test 1", "test 1", p1Positions, null, p1Rules.values());

    // The Portfolio has a weighted average rule requiring maximum duration = 3.5. Its current
    // weighted average should be 3.0, with weight 1_000_000. The proposed security has duration
    // 4.0, so the Portfolio should have room for 1_000_000 (+/- specified tolerance).

    EvaluationContext evaluationContext = TestUtil.defaultTestEvaluationContext();
    assertEquals(3.0, new WeightedAveragePositionCalculator<>(SecurityAttribute.duration)
            .calculate(portfolio.getPositionsWithContext(evaluationContext)), TestUtil.EPSILON,
        "unexpected current Portfolio duration");
    double room =
        new LocalTradeEvaluator(new LocalPortfolioEvaluator(TestUtil.testPortfolioProvider()),
            TestUtil.testPortfolioProvider(), TestUtil.testSecurityProvider())
            .evaluateRoom(portfolio.getKey(), trialSecurity.getKey(), 3_000_000).get();
    assertEquals(1_000_000, room, LocalTradeEvaluator.ROOM_TOLERANCE, "unexpected room result");

    // perform the same test with the clustered evaluator

    security1 =
        TestUtil.createTestSecurity(assetCache, "TradeEvaluatorTestSec3", security1Attributes);
    position1 = TestUtil.createTestPosition(assetCache, 1_000_000, security1);
    p1Positions = Set.of(position1);
    rule1 = TestRuleProvider
        .createTestWeightedAverageRule(assetCache, null, "weighted average: duration <= 3.5", null,
            SecurityAttribute.duration, null, 3.5, null);
    Map<RuleKey, ? extends ConfigurableRule> cachedP1Rules = Map.of(rule1.getKey(), rule1);
    trialSecurity =
        TestUtil.createTestSecurity(assetCache, "TradeEvaluatorTestSec4", security2Attributes);
    portfolio = TestUtil
        .createTestPortfolio(assetCache, null, "test 1", p1Positions, null, cachedP1Rules.values());

    room = new LocalTradeEvaluator(clusterEvaluator, assetCache)
        .evaluateRoom(portfolio.getKey(), trialSecurity.getKey(), 3_000_000).get();
    assertEquals(1_000_000, room, LocalTradeEvaluator.ROOM_TOLERANCE, "unexpected room result");
  }
}
