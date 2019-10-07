package org.slaq.slaqworx.panoptes.trade;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleResult;

/**
 * {@code TradeEvaluationResultTest} tests the functionality of the {@code TradeEvaluationResult}.
 *
 * @author jeremy
 */
public class TradeEvaluationResultTest {
    /**
     * Tests that {@code addImpacts()} behaves as expected.
     */
    @Test
    public void testAddImpacts() {
        // Portfolios, Rules and EvaluationGroups can be bogus for this test
        PortfolioKey portfolioKey = new PortfolioKey("test", 1);
        RuleKey rule1Key = new RuleKey("rule1");
        RuleKey rule2Key = new RuleKey("rule2");
        EvaluationGroup<Integer> evalGroup1 = new EvaluationGroup<>("group1", 1);
        EvaluationGroup<Integer> evalGroup2 = new EvaluationGroup<>("group2", 2);
        RuleResult PASS = new RuleResult(true);
        RuleResult FAIL = new RuleResult(false);

        // result grouping may vary between current and proposed states, regardless of evaluation
        // mode
        EvaluationResult rule1Results =
                new EvaluationResult(rule1Key, Map.of(evalGroup1, PASS), Map.of(evalGroup2, PASS));
        EvaluationResult rule2Results = new EvaluationResult(rule2Key,
                Map.of(evalGroup1, PASS, evalGroup2, PASS), Map.of(evalGroup2, PASS));

        Map<RuleKey, EvaluationResult> ruleResults =
                Map.of(rule1Key, rule1Results, rule2Key, rule2Results);

        // Portfolio was compliant before and after, which implies Trade compliance
        TradeEvaluationResult tradeResult = new TradeEvaluationResult();
        tradeResult.addImpacts(portfolioKey, ruleResults);
        assertTrue(tradeResult.isCompliant(), "pass->pass should be compliant");

        // a failure in the current state with a corresponding pass (or at least non-failure) in the
        // proposed state also implies Trade compliance
        rule1Results =
                new EvaluationResult(rule1Key, Map.of(evalGroup1, FAIL), Map.of(evalGroup2, PASS));
        ruleResults = Map.of(rule1Key, rule1Results, rule2Key, rule2Results);
        tradeResult = new TradeEvaluationResult();
        tradeResult.addImpacts(portfolioKey, ruleResults);
        assertTrue(tradeResult.isCompliant(), "fail->pass should be compliant");

        // a pass in the current state with a corresponding fail in the proposed state also implies
        // Trade compliance
        rule1Results =
                new EvaluationResult(rule1Key, Map.of(evalGroup1, PASS), Map.of(evalGroup2, FAIL));
        ruleResults = Map.of(rule1Key, rule1Results, rule2Key, rule2Results);
        tradeResult = new TradeEvaluationResult();
        tradeResult.addImpacts(portfolioKey, ruleResults);
        assertFalse(tradeResult.isCompliant(), "pass->fail should be compliant");
    }
}
