package org.slaq.slaqworx.panoptes.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.datamodel.Tuple3;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.event.PortfolioEvaluationInput;
import org.slaq.slaqworx.panoptes.pipeline.EvaluationRequestGenerator.EvaluationRequestGeneratorState;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;

/**
 * Tests the functionality of the {@link EvaluationRequestGenerator}.
 */
public class EvaluationRequestGeneratorTest {
  /**
   * Tests that the {@link EvaluationRequestGenerator} behaves as expected.
   */
  @Test
  public void testGenerator() {
    EvaluationRequestGenerator generator = new EvaluationRequestGenerator();
    EvaluationRequestGeneratorState state = generator.getEx();

    PortfolioKey portfolioKey = new PortfolioKey("test", 1);

    RuleKey ruleKey = new RuleKey("test");
    WeightedAverageRule rule =
        new WeightedAverageRule(ruleKey, "test portfolio rule", null, SecurityAttribute.duration,
            null, null, null);

    // associate the rule with the portfolio

    Traverser<PortfolioEvaluationInput> output = generator.handleRuleEvent(state,
        Tuple3.tuple3(EvaluationSource.PORTFOLIO, portfolioKey, rule));
    assertNull(output.next(), "should not have output yet");

    output = generator.handleEvaluationEvent(state, portfolioKey);
    PortfolioEvaluationInput item = output.next();
    assertNotNull(item, "output should have emitted portfolio event");
    assertEquals(portfolioKey, item.getPortfolioKey(), "event should have been on portfolio");
    assertEquals(EvaluationSource.PORTFOLIO, item.getEvaluationSource(),
        "event should have had PORTFOLIO source");
    ArrayList<Rule> rules = item.getRules();
    assertEquals(1, rules.size(), "unexpected number of rules");
    assertEquals(rule, rules.get(0), "rule should have matched associated rule");
    assertNull(output.next(), "output should have emitted exactly one item");
  }
}
