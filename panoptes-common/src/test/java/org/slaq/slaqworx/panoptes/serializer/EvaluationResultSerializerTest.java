package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;

/**
 * Tests the functionality of the {@link EvaluationResultSerializer}.
 *
 * @author jeremy
 */
public class EvaluationResultSerializerTest {
  /**
   * Tests that (de)serialization works as expected.
   *
   * @throws Exception if an unexpected error occurs
   */
  @Test
  public void testSerialization() throws Exception {
    EvaluationResultSerializer serializer = new EvaluationResultSerializer();

    RuleKey ruleKey = new RuleKey(null);
    EvaluationGroup defaultGroup = EvaluationGroup.defaultGroup();
    EvaluationGroup group1 = new EvaluationGroup("group1", "group1Key");
    EvaluationGroup group2 = new EvaluationGroup("group2", "group2Key");

    ValueResult defaultResult = new ValueResult(Threshold.BELOW, 2d);
    ValueResult group1Result = new ValueResult(false);
    ValueResult group2Result = new ValueResult(Threshold.WITHIN, 1d);
    Map<EvaluationGroup, ValueResult> results =
        Map.of(defaultGroup, defaultResult, group1, group1Result, group2, group2Result);

    ValueResult proposedDefaultResult = new ValueResult(Threshold.WITHIN, 4d);
    ValueResult proposedGroup1Result = new ValueResult(true);
    ValueResult proposedGroup2Result = new ValueResult(Threshold.ABOVE, 3d);
    Map<EvaluationGroup, ValueResult> proposedResults =
        Map.of(
            defaultGroup,
            proposedDefaultResult,
            group1,
            proposedGroup1Result,
            group2,
            proposedGroup2Result);

    EvaluationResult result = new EvaluationResult(ruleKey, results, proposedResults);

    byte[] buffer = serializer.write(result);
    EvaluationResult deserialized = serializer.read(buffer);

    assertEquals(
        ruleKey, deserialized.getRuleKey(), "deserialized RuleKey should equals() original");

    assertEquals(
        results.size(),
        deserialized.results().size(),
        "deserialized results should have same size as original");
    assertEquals(
        defaultResult, deserialized.getResult(defaultGroup), "unexpected result for default group");
    assertEquals(group1Result, deserialized.getResult(group1), "unexpected result for group 1");
    assertEquals(group2Result, deserialized.getResult(group2), "unexpected result for group 2");

    assertEquals(
        proposedResults.size(),
        deserialized.proposedResults().size(),
        "deserialized proposed results should have same size as original");
    assertEquals(
        proposedDefaultResult,
        deserialized.getProposedResult(defaultGroup),
        "unexpected proposed result for default group");
    assertEquals(
        proposedGroup1Result,
        deserialized.getProposedResult(group1),
        "unexpected proposed result for group 1");
    assertEquals(
        proposedGroup2Result,
        deserialized.getProposedResult(group2),
        "unexpected proposed result for group 2");
  }
}
