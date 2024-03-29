package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Impact;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * Tests the functionality of the {@link TradeEvaluationResultSerializer}.
 *
 * @author jeremy
 */
public class TradeEvaluationResultSerializerTest {
  /**
   * Tests that (de)serialization works as expected.
   *
   * @throws Exception if an unexpected error occurs
   */
  @Test
  public void testSerialization() throws Exception {
    TradeEvaluationResultSerializer serializer = new TradeEvaluationResultSerializer();

    TradeKey tradeKey = new TradeKey("trade1");

    PortfolioKey portfolio1Key = new PortfolioKey(null, 1);
    RuleKey rule1aKey = new RuleKey(null);
    RuleKey rule1bKey = new RuleKey(null);

    PortfolioKey portfolio2Key = new PortfolioKey(null, 1);
    RuleKey rule2aKey = new RuleKey(null);
    RuleKey rule2bKey = new RuleKey(null);

    EvaluationGroup defaultGroup = EvaluationGroup.defaultGroup();
    EvaluationGroup group1 = new EvaluationGroup("group1", "group1Key");
    EvaluationGroup group2 = new EvaluationGroup("group2", "group2Key");

    TradeEvaluationResult result = new TradeEvaluationResult(tradeKey);
    result.addImpact(portfolio1Key, rule1aKey, defaultGroup, Impact.NEGATIVE);
    result.addImpact(portfolio1Key, rule1aKey, group1, Impact.NEUTRAL);
    result.addImpact(portfolio1Key, rule1aKey, group2, Impact.POSITIVE);
    result.addImpact(portfolio1Key, rule1bKey, defaultGroup, Impact.NEGATIVE);
    result.addImpact(portfolio2Key, rule2aKey, defaultGroup, Impact.NEUTRAL);
    result.addImpact(portfolio2Key, rule2aKey, group1, Impact.NEUTRAL);
    result.addImpact(portfolio2Key, rule2aKey, group2, Impact.NEUTRAL);
    result.addImpact(portfolio2Key, rule2bKey, defaultGroup, Impact.NEUTRAL);

    byte[] buffer = serializer.write(result);
    TradeEvaluationResult deserialized = serializer.read(buffer);

    assertEquals(tradeKey, deserialized.getTradeKey(), "deserialized key should equals() original");
    assertEquals(
        result.getImpacts(),
        deserialized.getImpacts(),
        "deserialized impacts should equals() original");
  }
}
