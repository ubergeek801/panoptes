package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleResult;
import org.slaq.slaqworx.panoptes.rule.RuleResult.Threshold;

/**
 * {@code EvaluationResultSerializerTest} tests the functionality of the
 * {@code EvaluationResultSerializer}.
 *
 * @author jeremy
 */
public class EvaluationResultSerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        EvaluationResultSerializer serializer = new EvaluationResultSerializer();

        RuleKey ruleKey = new RuleKey(null);
        EvaluationGroup defaultGroup = EvaluationGroup.defaultGroup();
        EvaluationGroup group1 = new EvaluationGroup("group1", "group1Key");
        EvaluationGroup group2 = new EvaluationGroup("group2", "group2Key");

        RuleResult defaultResult = new RuleResult(Threshold.BELOW, 2d);
        RuleResult group1Result = new RuleResult(false);
        RuleResult group2Result = new RuleResult(Threshold.WITHIN, 1d, 2d);
        Map<EvaluationGroup, RuleResult> results =
                Map.of(defaultGroup, defaultResult, group1, group1Result, group2, group2Result);

        RuleResult proposedDefaultResult = new RuleResult(Threshold.WITHIN, 4d);
        RuleResult proposedGroup1Result = new RuleResult(true);
        RuleResult proposedGroup2Result = new RuleResult(Threshold.ABOVE, 3d, 2d);
        Map<EvaluationGroup, RuleResult> proposedResults = Map.of(defaultGroup,
                proposedDefaultResult, group1, proposedGroup1Result, group2, proposedGroup2Result);

        EvaluationResult result = new EvaluationResult(ruleKey, results, proposedResults);

        byte[] buffer = serializer.write(result);
        EvaluationResult deserialized = serializer.read(buffer);

        assertEquals(ruleKey, deserialized.getRuleKey(),
                "deserialized RuleKey should equals() original");

        assertEquals(results.size(), deserialized.getResults().size(),
                "deserialized results should have same size as original");
        assertEquals(defaultResult, deserialized.getResult(defaultGroup),
                "unexpected result for default group");
        assertEquals(group1Result, deserialized.getResult(group1), "unexpected result for group 1");
        assertEquals(group2Result, deserialized.getResult(group2), "unexpected result for group 2");

        assertEquals(proposedResults.size(), deserialized.getProposedResults().size(),
                "deserialized proposed results should have same size as original");
        assertEquals(proposedDefaultResult, deserialized.getProposedResult(defaultGroup),
                "unexpected proposed result for default group");
        assertEquals(proposedGroup1Result, deserialized.getProposedResult(group1),
                "unexpected proposed result for group 1");
        assertEquals(proposedGroup2Result, deserialized.getProposedResult(group2),
                "unexpected proposed result for group 2");
    }
}
