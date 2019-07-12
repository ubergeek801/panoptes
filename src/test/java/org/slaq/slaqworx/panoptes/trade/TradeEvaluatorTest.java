package org.slaq.slaqworx.panoptes.trade;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;

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
    public void testEvaluate() {
        Security s1 = new Security("s1", Map.of(SecurityAttribute.duration, 3.0));
        Security s2 = new Security("s2", Map.of(SecurityAttribute.duration, 4.0));

        HashSet<Position> p1Positions = new HashSet<>();
        p1Positions.add(new Position(1_000, s1));

        // to keep things simple, all Rules test against duration, with some conflicting
        // assertions
        HashSet<Rule> p1Rules = new HashSet<>();
        // since the Portfolio holds only a 3.0 Security, this Rule should be satisfied
        Rule p1Rule1 = new WeightedAverageRule("p1Rule1", "WeightedAverage<=3.0", null,
                SecurityAttribute.duration, null, 3d, null);
        p1Rules.add(p1Rule1);
        // since the Portfolio is already above the limit, this Rule should fail
        Rule p1Rule2 = new WeightedAverageRule("p1Rule2", "WeightedAverage<=2.0", null,
                SecurityAttribute.duration, null, 2d, null);
        p1Rules.add(p1Rule2);
        // since the Portfolio is already below the limit, this Rule should fail
        Rule p1Rule3 = new WeightedAverageRule("p1Rule3", "WeightedAverage>=4.0", null,
                SecurityAttribute.duration, 4d, null, null);
        p1Rules.add(p1Rule3);

        Portfolio p1 = new Portfolio("p1", p1Positions, null, p1Rules);

        ArrayList<Transaction> transactions = new ArrayList<>();

        ArrayList<Position> t1Allocations = new ArrayList<>();
        // adding a 4.0 Security should cause p1Rule1 to fail since it was already at the limit
        Position t1Alloc1 = new Position(1_000, s2);
        t1Allocations.add(t1Alloc1);
        Transaction t1 = new Transaction(p1, t1Allocations);
        transactions.add(t1);

        Trade trade = new Trade(transactions);
        TradeEvaluator evaluator = new TradeEvaluator();
        TradeEvaluationResult result = evaluator.evaluate(trade);

        // FIXME implement some tests
        fail("implement testEvaluate()");
    }
}
