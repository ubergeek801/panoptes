package org.slaq.slaqworx.panoptes.evaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.GenericRule;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleResult;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;

/**
 * {@code RuleEvaluatorTest} tests the functionality of the {@code RuleEvaluator}.
 *
 * @author jeremy
 */
public class RuleEvaluatorTest {
    /**
     * Tests that position classification behaves as expected.
     */
    @Test
    public void testClassify() {
        // a dumb classifier that merely "classifies" by security ID
        EvaluationGroupClassifier classifier =
                (p -> new EvaluationGroup(p.getSecurity().getKey().getId(), "id"));
        Rule rule = new GenericRule(null, "dummy rule", classifier) {
            @Override
            protected RuleResult eval(PositionSupplier portfolioPositions,
                    PositionSupplier benchmarkPositions, EvaluationContext evaluationContext) {
                // will not actually be invoked anyway
                return null;
            }
        };
        Set<Position> positions = TestUtil.p1Positions;

        EvaluationContext evaluationContext = new EvaluationContext();
        Map<EvaluationGroup, Collection<Position>> classifiedPositions =
                new RuleEvaluator(rule, null, null, new EvaluationContext()).classify(positions
                        .stream().map(p -> new PositionEvaluationContext(p, evaluationContext)));
        assertEquals(2, classifiedPositions.size(),
                "Positions in distinct Securities should have been classified distinctly");
    }

    /**
     * Tests that position evaluation behaves as expected.
     */
    @Test
    public void testEvaluate() {
        // a dumb classifier that merely "classifies" by security ID
        EvaluationGroupClassifier classifier =
                (p -> new EvaluationGroup(p.getSecurity().getKey().getId(), "id"));
        // a dumb filter that matches Positions in s1
        Predicate<PositionEvaluationContext> filter =
                (c -> c.getPosition().getSecurity().getKey().equals(TestUtil.s1.getKey()));
        Rule rule = new WeightedAverageRule(null, "test rule", filter, SecurityAttribute.duration,
                0.8, 1.2, classifier);

        EvaluationResult result =
                new RuleEvaluator(rule, TestUtil.p1, null, new EvaluationContext()).call();
        assertNotNull(result, "result should never be null");
        Map<EvaluationGroup, RuleResult> groupedResults = result.getResults();
        assertNotNull(groupedResults, "result groups should never be null");
        assertEquals(1, groupedResults.size(),
                "a single (filtered) Position should result in a single group");
    }
}
