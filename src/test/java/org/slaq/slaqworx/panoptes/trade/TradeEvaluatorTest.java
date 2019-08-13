package org.slaq.slaqworx.panoptes.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult.Impact;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult.PortfolioRuleKey;

/**
 * TradeEvaluatorTest tests the functionality of the TradeEvaluator.
 *
 * @author jeremy
 */
public class TradeEvaluatorTest {
    /**
     * Tests that evaluate() behaves as expected.
     */
    @Test
    public void testEvaluate() throws Exception {
        TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

        Security s1 = securityProvider.newSecurity(Map.of(TestUtil.duration, 3.0));
        Security s2 = securityProvider.newSecurity(Map.of(TestUtil.duration, 4.0));

        HashSet<Position> p1Positions = new HashSet<>();
        p1Positions.add(new MaterializedPosition(1_000, s1.getKey()));

        // to keep things simple, all Rules test against duration, with some conflicting
        // assertions
        HashMap<RuleKey, Rule> p1Rules = new HashMap<>();
        // since the Portfolio is already within (at) the limit, and the Trade pushes it out, this
        // Rule should fail
        Rule p1Rule1 = new WeightedAverageRule(new RuleKey("p1Rule1"), "WeightedAverage<=3.0", null,
                TestUtil.duration, null, 3d, null);
        p1Rules.put(p1Rule1.getKey(), p1Rule1);
        // since the Portfolio is already above the limit, and the Trade makes it worse, this Rule
        // should fail
        Rule p1Rule2 = new WeightedAverageRule(new RuleKey("p1Rule2"), "WeightedAverage<=2.0", null,
                TestUtil.duration, null, 2d, null);
        p1Rules.put(p1Rule2.getKey(), p1Rule2);
        // since the Portfolio is already below the limit, and the Trade improves it, this Rule
        // should pass
        Rule p1Rule3 = new WeightedAverageRule(new RuleKey("p1Rule3"), "WeightedAverage>=4.0", null,
                TestUtil.duration, 4d, null, null);
        p1Rules.put(p1Rule3.getKey(), p1Rule3);
        // since the Portfolio is already within the limit, and remains so with the Trade, this Rule
        // should pass
        Rule p1Rule4 = new WeightedAverageRule(new RuleKey("p1Rule4"), "WeightedAverage<=4.0", null,
                TestUtil.duration, null, 4d, null);
        p1Rules.put(p1Rule4.getKey(), p1Rule4);

        RuleProvider ruleProvider = (k -> p1Rules.get(k));

        Portfolio p1 = new Portfolio(new PortfolioKey("p1", 1), "test", p1Positions,
                (PortfolioKey)null, p1Rules.values());

        MaterializedPosition t1Alloc1 = new MaterializedPosition(1_000, s2.getKey());
        List<Position> t1Allocations = Arrays.asList(t1Alloc1);
        Transaction t1 = new Transaction(p1, t1Allocations);
        List<Transaction> transactions = Arrays.asList(t1);

        Trade trade = new Trade(transactions);
        TradeEvaluator evaluator = new TradeEvaluator(null, securityProvider, ruleProvider);
        TradeEvaluationResult result = evaluator.evaluate(trade);

        Map<EvaluationGroup<?>, Impact> p1r1Impact =
                result.getImpacts().get(new PortfolioRuleKey(p1.getKey(), p1Rule1.getKey()));
        assertNotNull(p1r1Impact, "should have found impact for p1Rule1");
        assertEquals(Impact.NEGATIVE, p1r1Impact.get(EvaluationGroup.defaultGroup()),
                "p1Rule1 should have failed");

        Map<EvaluationGroup<?>, Impact> p1r2Impact =
                result.getImpacts().get(new PortfolioRuleKey(p1.getKey(), p1Rule2.getKey()));
        assertNotNull(p1r2Impact, "should have found impact for p1Rule2");
        assertEquals(Impact.NEGATIVE, p1r2Impact.get(EvaluationGroup.defaultGroup()),
                "p1Rule2 should have failed");

        Map<EvaluationGroup<?>, Impact> p1r3Impact =
                result.getImpacts().get(new PortfolioRuleKey(p1.getKey(), p1Rule3.getKey()));
        assertNotNull(p1r3Impact, "should have found impact for p1Rule3");
        assertEquals(Impact.POSITIVE, p1r3Impact.get(EvaluationGroup.defaultGroup()),
                "p1Rule3 should have passed");

        Map<EvaluationGroup<?>, Impact> p1r4Impact =
                result.getImpacts().get(new PortfolioRuleKey(p1.getKey(), p1Rule4.getKey()));
        assertNotNull(p1r4Impact, "should have found impact for p1Rule4");
        assertEquals(Impact.NEUTRAL, p1r4Impact.get(EvaluationGroup.defaultGroup()),
                "p1Rule4 should have passed");
    }
}
