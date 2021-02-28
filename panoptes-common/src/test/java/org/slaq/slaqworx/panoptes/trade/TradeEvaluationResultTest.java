package org.slaq.slaqworx.panoptes.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Impact;

/**
 * Tests the functionality of the {@link TradeEvaluationResult}.
 *
 * @author jeremy
 */
public class TradeEvaluationResultTest {
  /**
   * Tests that {@code addImpacts()} behaves as expected.
   */
  @Test
  public void testAddImpacts() {
    TradeKey tradeKey = new TradeKey("trade1");

    // Portfolios, Rules and EvaluationGroups can be bogus for this test
    PortfolioKey portfolioKey = new PortfolioKey("test", 1);
    RuleKey rule1Key = new RuleKey("rule1");
    RuleKey rule2Key = new RuleKey("rule2");
    EvaluationGroup evalGroup1 = new EvaluationGroup("group1", "1");
    EvaluationGroup evalGroup2 = new EvaluationGroup("group2", "2");
    ValueResult PASS = new ValueResult(true);
    ValueResult FAIL = new ValueResult(false);

    // result grouping may vary between current and proposed states, regardless of evaluation
    // mode
    EvaluationResult rule1Results =
        new EvaluationResult(rule1Key, Map.of(evalGroup1, PASS), Map.of(evalGroup2, PASS));
    EvaluationResult rule2Results =
        new EvaluationResult(rule2Key, Map.of(evalGroup1, PASS, evalGroup2, PASS),
            Map.of(evalGroup2, PASS));

    Map<RuleKey, EvaluationResult> ruleResults =
        Map.of(rule1Key, rule1Results, rule2Key, rule2Results);

    // Portfolio was compliant before and after, which implies Trade compliance
    TradeEvaluationResult tradeResult = new TradeEvaluationResult(tradeKey);
    tradeResult.addImpacts(portfolioKey, ruleResults);
    assertTrue(tradeResult.isCompliant(), "pass->pass should be compliant");

    // a failure in the current state with a corresponding pass (or at least non-failure) in the
    // proposed state also implies Trade compliance
    rule1Results =
        new EvaluationResult(rule1Key, Map.of(evalGroup1, FAIL), Map.of(evalGroup2, PASS));
    ruleResults = Map.of(rule1Key, rule1Results, rule2Key, rule2Results);
    tradeResult = new TradeEvaluationResult(tradeKey);
    tradeResult.addImpacts(portfolioKey, ruleResults);
    assertTrue(tradeResult.isCompliant(), "fail->pass should be compliant");

    // a pass in the current state with a corresponding fail in the proposed state also implies
    // Trade compliance
    rule1Results =
        new EvaluationResult(rule1Key, Map.of(evalGroup1, PASS), Map.of(evalGroup2, FAIL));
    ruleResults = Map.of(rule1Key, rule1Results, rule2Key, rule2Results);
    tradeResult = new TradeEvaluationResult(tradeKey);
    tradeResult.addImpacts(portfolioKey, ruleResults);
    assertFalse(tradeResult.isCompliant(), "pass->fail should be compliant");
  }

  /**
   * Tests that {@code equals()} behaves as expected.
   */
  @Test
  public void testEquals() {
    TradeKey tradeKey = new TradeKey("trade1");

    // Portfolios, Rules and EvaluationGroups can be bogus for this test
    PortfolioKey portfolio1Key = new PortfolioKey("test1", 1);
    PortfolioKey portfolio2Key = new PortfolioKey("test2", 1);
    RuleKey rule1Key = new RuleKey("rule1");
    RuleKey rule2Key = new RuleKey("rule2");
    EvaluationGroup evalGroup1 = new EvaluationGroup("group1", "1");
    EvaluationGroup evalGroup2 = new EvaluationGroup("group2", "2");

    TradeEvaluationResult result1 = new TradeEvaluationResult(tradeKey);
    result1.addImpact(portfolio1Key, rule1Key, evalGroup1, Impact.POSITIVE);
    result1.addImpact(portfolio1Key, rule1Key, evalGroup2, Impact.NEGATIVE);
    result1.addImpact(portfolio2Key, rule2Key, evalGroup1, Impact.POSITIVE);
    result1.addImpact(portfolio2Key, rule2Key, evalGroup2, Impact.NEGATIVE);
    TradeEvaluationResult result1a = new TradeEvaluationResult(tradeKey);
    result1a.addImpact(portfolio1Key, rule1Key, evalGroup1, Impact.POSITIVE);
    result1a.addImpact(portfolio1Key, rule1Key, evalGroup2, Impact.NEGATIVE);
    result1a.addImpact(portfolio2Key, rule2Key, evalGroup1, Impact.POSITIVE);
    result1a.addImpact(portfolio2Key, rule2Key, evalGroup2, Impact.NEGATIVE);
    TradeEvaluationResult result2 = new TradeEvaluationResult(tradeKey);
    result2.addImpact(portfolio1Key, rule2Key, evalGroup1, Impact.POSITIVE);
    result2.addImpact(portfolio1Key, rule2Key, evalGroup2, Impact.NEGATIVE);
    result2.addImpact(portfolio2Key, rule1Key, evalGroup1, Impact.POSITIVE);
    result2.addImpact(portfolio2Key, rule1Key, evalGroup2, Impact.NEGATIVE);

    assertEquals(result1, result1, "result should equals() itself");
    assertEquals(result1, result1a, "result should equals() identical result");
    assertNotEquals(result1, result2, "results with different contents should not be equal");
    assertNotEquals(null, result1, "result should not equals() null");
  }

  /**
   * Tests that {@code isCompliant()} behaves as expected.
   */
  @Test
  public void testIsCompliant() {
    TradeKey tradeKey = new TradeKey("trade1");

    TradeEvaluationResult result = new TradeEvaluationResult(tradeKey);

    assertTrue(result.isCompliant(), "empty result should be compliant");

    PortfolioKey portfolioKey = new PortfolioKey("test1", 1);
    RuleKey rule1Key = new RuleKey("rule1");
    RuleKey rule2Key = new RuleKey("rule2");
    EvaluationGroup evalGroup1 = new EvaluationGroup("group1", "1");
    EvaluationGroup evalGroup2 = new EvaluationGroup("group2", "2");

    result.addImpact(portfolioKey, rule1Key, evalGroup1, Impact.POSITIVE);
    assertTrue(result.isCompliant(), "result with only positive Impacts should be compliant");

    result.addImpact(portfolioKey, rule1Key, evalGroup2, Impact.NEUTRAL);
    assertTrue(result.isCompliant(),
        "result with only positive and neutral Impacts should be compliant");

    result.addImpact(portfolioKey, rule2Key, evalGroup1, Impact.UNKNOWN);
    assertFalse(result.isCompliant(), "result with an unknown Impact should not be compliant");

    result.addImpact(portfolioKey, rule2Key, evalGroup2, Impact.NEGATIVE);
    assertFalse(result.isCompliant(), "result with a negative Impact should not be compliant");
  }

  /**
   * Tests that {@code merge()} behaves as expected.
   */
  @Test
  public void testMerge() {
    TradeKey tradeKey = new TradeKey("trade1");

    // Portfolios, Rules and EvaluationGroups can be bogus for this test
    PortfolioKey portfolio1Key = new PortfolioKey("test1", 1);
    PortfolioKey portfolio2Key = new PortfolioKey("test2", 1);
    RuleKey rule1Key = new RuleKey("rule1");
    RuleKey rule2Key = new RuleKey("rule2");
    EvaluationGroup evalGroup1 = new EvaluationGroup("group1", "1");
    EvaluationGroup evalGroup2 = new EvaluationGroup("group2", "2");

    TradeEvaluationResult result1 = new TradeEvaluationResult(tradeKey);
    result1.addImpact(portfolio1Key, rule1Key, evalGroup1, Impact.POSITIVE);
    result1.addImpact(portfolio1Key, rule1Key, evalGroup2, Impact.NEGATIVE);
    result1.addImpact(portfolio2Key, rule2Key, evalGroup1, Impact.POSITIVE);
    result1.addImpact(portfolio2Key, rule2Key, evalGroup2, Impact.NEGATIVE);
    TradeEvaluationResult result2 = new TradeEvaluationResult(tradeKey);
    result2.addImpact(portfolio1Key, rule2Key, evalGroup1, Impact.POSITIVE);
    result2.addImpact(portfolio1Key, rule2Key, evalGroup2, Impact.NEGATIVE);
    result2.addImpact(portfolio2Key, rule1Key, evalGroup1, Impact.POSITIVE);
    result2.addImpact(portfolio2Key, rule1Key, evalGroup2, Impact.NEGATIVE);

    TradeEvaluationResult merged = result1.merge(result2);
    Map<PortfolioRuleKey, Map<EvaluationGroup, Impact>> allImpacts = merged.getImpacts();

    assertNotNull(allImpacts, "should have obtained Impact results");
    // 2 Portfolios * 2 Rules = 4 impact results
    assertEquals(4, allImpacts.size(), "unexpected number of PortfolioRule results");

    PortfolioRuleKey portfolio1Rule1Key = new PortfolioRuleKey(portfolio1Key, rule1Key);
    Map<EvaluationGroup, Impact> groupImpacts = allImpacts.get(portfolio1Rule1Key);
    assertNotNull(groupImpacts, "should have found group Impacts for Portfolio 1 and Rule 1");
    Impact impact = groupImpacts.get(evalGroup1);
    assertEquals(Impact.POSITIVE, impact, "unexpected Impact for EvaluationGroup 1");
    impact = groupImpacts.get(evalGroup2);
    assertEquals(Impact.NEGATIVE, impact, "unexpected Impact for EvaluationGroup 2");

    PortfolioRuleKey portfolio1Rule2Key = new PortfolioRuleKey(portfolio1Key, rule2Key);
    groupImpacts = allImpacts.get(portfolio1Rule2Key);
    assertNotNull(groupImpacts, "should have found group Impacts for Portfolio 1 and Rule 2");
    impact = groupImpacts.get(evalGroup1);
    assertEquals(Impact.POSITIVE, impact, "unexpected Impact for EvaluationGroup 1");
    impact = groupImpacts.get(evalGroup2);
    assertEquals(Impact.NEGATIVE, impact, "unexpected Impact for EvaluationGroup 2");

    PortfolioRuleKey portfolio2Rule1Key = new PortfolioRuleKey(portfolio2Key, rule1Key);
    groupImpacts = allImpacts.get(portfolio2Rule1Key);
    assertNotNull(groupImpacts, "should have found group Impacts for Portfolio 2 and Rule 1");
    impact = groupImpacts.get(evalGroup1);
    assertEquals(Impact.POSITIVE, impact, "unexpected Impact for EvaluationGroup 1");
    impact = groupImpacts.get(evalGroup2);
    assertEquals(Impact.NEGATIVE, impact, "unexpected Impact for EvaluationGroup 2");

    PortfolioRuleKey portfolio2Rule2Key = new PortfolioRuleKey(portfolio2Key, rule2Key);
    groupImpacts = allImpacts.get(portfolio2Rule2Key);
    assertNotNull(groupImpacts, "should have found group Impacts for Portfolio 2 and Rule 2");
    impact = groupImpacts.get(evalGroup1);
    assertEquals(Impact.POSITIVE, impact, "unexpected Impact for EvaluationGroup 1");
    impact = groupImpacts.get(evalGroup2);
    assertEquals(Impact.NEGATIVE, impact, "unexpected Impact for EvaluationGroup 2");
  }
}
