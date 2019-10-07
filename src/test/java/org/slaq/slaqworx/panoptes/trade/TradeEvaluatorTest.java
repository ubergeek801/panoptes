package org.slaq.slaqworx.panoptes.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.micronaut.test.annotation.MicronautTest;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.calc.WeightedAveragePositionCalculator;
import org.slaq.slaqworx.panoptes.evaluator.ClusterPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.evaluator.LocalPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.TradeEvaluationMode;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult.Impact;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult.PortfolioRuleKey;

/**
 * {@code TradeEvaluatorTest} tests the functionality of the {@code TradeEvaluator}.
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
     * Tests that {@code addImpacts()} behaves as expected.
     */
    @Test
    public void testAddImpacts() {
        TradeEvaluator evaluator = new TradeEvaluator(null, null, null, null);
        // Portfolios, Rules and EvaluationGroups can be bogus for this test
        PortfolioKey portfolioKey = new PortfolioKey("test", 1);
        RuleKey rule1Key = new RuleKey("rule1");
        RuleKey rule2Key = new RuleKey("rule2");
        EvaluationGroup<Integer> evalGroup1 = new EvaluationGroup<>("group1", 1);
        EvaluationGroup<Integer> evalGroup2 = new EvaluationGroup<>("group2", 2);
        EvaluationResult PASS = new EvaluationResult(true);
        EvaluationResult FAIL = new EvaluationResult(false);

        // result grouping may vary between current and proposed states, regardless of evaluation
        // mode
        Map<EvaluationGroup<?>, EvaluationResult> rule1CurrentResults = Map.of(evalGroup1, PASS);
        Map<EvaluationGroup<?>, EvaluationResult> rule2CurrentResults =
                Map.of(evalGroup1, PASS, evalGroup2, PASS);
        Map<EvaluationGroup<?>, EvaluationResult> rule1ProposedResults = Map.of(evalGroup2, PASS);
        Map<EvaluationGroup<?>, EvaluationResult> rule2ProposedResults = Map.of(evalGroup2, PASS);

        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> currentState =
                Map.of(rule1Key, rule1CurrentResults, rule2Key, rule2CurrentResults);
        Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> proposedState =
                Map.of(rule1Key, rule1ProposedResults, rule2Key, rule2ProposedResults);

        // Portfolio was compliant before and after, which implies Trade compliance regardless of
        // mode
        TradeEvaluationResult result = new TradeEvaluationResult();
        evaluator.addImpacts(portfolioKey, result, TradeEvaluationMode.FULL_EVALUATION,
                proposedState, currentState);
        assertTrue(result.isCompliant(), "pass->pass should be compliant");
        result = new TradeEvaluationResult();
        evaluator.addImpacts(portfolioKey, result,
                TradeEvaluationMode.FAIL_SHORT_CIRCUIT_EVALUATION, proposedState, currentState);
        assertTrue(result.isCompliant(), "pass->pass should be compliant");
        result = new TradeEvaluationResult();
        evaluator.addImpacts(portfolioKey, result,
                TradeEvaluationMode.PASS_SHORT_CIRCUIT_EVALUATION, proposedState, currentState);
        assertTrue(result.isCompliant(), "pass->pass should be compliant");

        // a failure in the current state with a corresponding pass (or at least non-failure) in the
        // proposed state also implies Trade compliance in all modes
        rule1CurrentResults = Map.of(evalGroup1, FAIL);
        currentState = Map.of(rule1Key, rule1CurrentResults, rule2Key, rule2CurrentResults);
        result = new TradeEvaluationResult();
        evaluator.addImpacts(portfolioKey, result, TradeEvaluationMode.FULL_EVALUATION,
                proposedState, currentState);
        assertTrue(result.isCompliant(), "fail->pass should be compliant");
        result = new TradeEvaluationResult();
        evaluator.addImpacts(portfolioKey, result,
                TradeEvaluationMode.FAIL_SHORT_CIRCUIT_EVALUATION, proposedState, currentState);
        assertTrue(result.isCompliant(), "fail->pass should be compliant");
        result = new TradeEvaluationResult();
        evaluator.addImpacts(portfolioKey, result,
                TradeEvaluationMode.PASS_SHORT_CIRCUIT_EVALUATION, proposedState, currentState);
        assertTrue(result.isCompliant(), "fail->pass should be compliant");

        // in a full evaluation, every Rule that was evaluated in the proposed state should also
        // have been evaluated in the current state, or else addImpacts() should produce an error
        currentState = Map.of(rule1Key, rule1CurrentResults); // no rule2 results
        result = new TradeEvaluationResult();
        try {
            evaluator.addImpacts(portfolioKey, result, TradeEvaluationMode.FULL_EVALUATION,
                    proposedState, currentState);
            fail("incomplete proposed state should cause an error");
        } catch (Exception expected) {
            // TODO expect a particular type of exception
        }

        // in a "pass" short-circuit evaluation, for all failed Rule evaluations in the proposed
        // state, only one needs to exist in the current state
        rule1ProposedResults = Map.of(evalGroup2, FAIL);
        rule2ProposedResults = Map.of(evalGroup2, FAIL);
        proposedState = Map.of(rule1Key, rule1ProposedResults, rule2Key, rule2ProposedResults);
        result = new TradeEvaluationResult();
        evaluator.addImpacts(portfolioKey, result,
                TradeEvaluationMode.PASS_SHORT_CIRCUIT_EVALUATION, proposedState, currentState);
        assertFalse(result.isCompliant(), "pass->fail should be noncompliant");
    }

    /**
     * Tests that {@code evaluate()} behaves as expected.
     */
    @Test
    public void testEvaluate() throws Exception {
        TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

        Security s1 = securityProvider.newSecurity("TradeEvaluatorTestSec1",
                Map.of(SecurityAttribute.duration, 3.0, SecurityAttribute.price,
                        new BigDecimal("100.00")));
        Security s2 = securityProvider.newSecurity("TradeEvaluatorTestSec2",
                Map.of(SecurityAttribute.duration, 4.0, SecurityAttribute.price,
                        new BigDecimal("100.00")));

        HashSet<Position> p1Positions = new HashSet<>();
        p1Positions.add(new Position(1_000, s1));

        // to keep things simple, all Rules test against duration, with some conflicting
        // assertions
        HashMap<RuleKey, ConfigurableRule> p1Rules = new HashMap<>();
        // since the Portfolio is already within (at) the limit, and the Trade pushes it out, this
        // Rule should fail
        ConfigurableRule p1Rule1 = new WeightedAverageRule(new RuleKey("p1Rule1"),
                "WeightedAverage<=3.0", null, SecurityAttribute.duration, null, 3d, null);
        p1Rules.put(p1Rule1.getKey(), p1Rule1);
        // since the Portfolio is already above the limit, and the Trade makes it worse, this Rule
        // should fail
        ConfigurableRule p1Rule2 = new WeightedAverageRule(new RuleKey("p1Rule2"),
                "WeightedAverage<=2.0", null, SecurityAttribute.duration, null, 2d, null);
        p1Rules.put(p1Rule2.getKey(), p1Rule2);
        // since the Portfolio is already below the limit, and the Trade improves it, this Rule
        // should pass
        ConfigurableRule p1Rule3 = new WeightedAverageRule(new RuleKey("p1Rule3"),
                "WeightedAverage>=4.0", null, SecurityAttribute.duration, 4d, null, null);
        p1Rules.put(p1Rule3.getKey(), p1Rule3);
        // since the Portfolio is already within the limit, and remains so with the Trade, this Rule
        // should pass
        ConfigurableRule p1Rule4 = new WeightedAverageRule(new RuleKey("p1Rule4"),
                "WeightedAverage<=4.0", null, SecurityAttribute.duration, null, 4d, null);
        p1Rules.put(p1Rule4.getKey(), p1Rule4);

        RuleProvider ruleProvider = (k -> p1Rules.get(k));

        Portfolio p1 = TestUtil.testPortfolioProvider().newPortfolio("TradeEvaluatorTestP1", "test",
                p1Positions, null, p1Rules.values());

        Position t1Alloc1 = new Position(1_000, s2);
        List<Position> t1Allocations = Arrays.asList(t1Alloc1);
        Transaction t1 = new Transaction(p1, t1Allocations);
        Map<PortfolioKey, Transaction> transactions = Map.of(t1.getPortfolio().getKey(), t1);

        Trade trade = new Trade(transactions);
        TradeEvaluator evaluator = new TradeEvaluator(new LocalPortfolioEvaluator(),
                TestUtil.testPortfolioProvider(), securityProvider, ruleProvider);
        TradeEvaluationResult result =
                evaluator.evaluate(trade, TradeEvaluationMode.FULL_EVALUATION);

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

        // in a short-circuit evaluation we won't know which rule would have failed, or what other
        // results might be included, but the overall result should be noncompliance

        result = evaluator.evaluate(trade, TradeEvaluationMode.FAIL_SHORT_CIRCUIT_EVALUATION);
        assertFalse(result.isCompliant(),
                "short-circuit evaluation should have yielded non-compliance");
    }

    /**
     * Tests that {@code evaluateRoom()} behaves as expected.
     */
    @Test
    public void testEvaluateRoom() throws Exception {
        Map<SecurityAttribute<?>, ? super Object> security1Attributes = Map.of(
                SecurityAttribute.duration, 3d, SecurityAttribute.price, new BigDecimal("1.00"));
        Security security1 = TestUtil.testSecurityProvider().newSecurity("TradeEvaluatorTestSec3",
                security1Attributes);

        Position position1 =
                TestUtil.testPositionProvider().newPosition(null, 1_000_000, security1);
        Set<Position> p1Positions = Set.of(position1);

        WeightedAverageRule rule1 =
                new WeightedAverageRule(null, "weighted average: duration <= 3.5", null,
                        SecurityAttribute.duration, null, 3.5, null);
        Map<RuleKey, ? extends ConfigurableRule> p1Rules = Map.of(rule1.getKey(), rule1);

        Map<SecurityAttribute<?>, ? super Object> security2Attributes = Map.of(
                SecurityAttribute.duration, 4d, SecurityAttribute.price, new BigDecimal("1.00"));
        Security trialSecurity = TestUtil.testSecurityProvider()
                .newSecurity("TradeEvaluatorTestSec4", security2Attributes);

        Portfolio portfolio = TestUtil.testPortfolioProvider().newPortfolio(null, "test 1",
                p1Positions, (Portfolio)null, p1Rules.values());

        // The Portfolio has a weighted average rule requiring maximum duration = 3.5. Its current
        // weighted average should be 3.0, with weight 1_000_000. The proposed security has duration
        // 4.0, so the Portfolio should have room for 1_000_000 (+/- specified tolerance).

        EvaluationContext evaluationContext =
                new EvaluationContext(TestUtil.testPortfolioProvider(),
                        TestUtil.testSecurityProvider(), k -> p1Rules.get(k));
        assertEquals(3.0,
                new WeightedAveragePositionCalculator(SecurityAttribute.duration)
                        .calculate(portfolio, evaluationContext),
                TestUtil.EPSILON, "unexpected current Portfolio duration");
        double room = new TradeEvaluator(new LocalPortfolioEvaluator(),
                evaluationContext.getPortfolioProvider(), evaluationContext.getSecurityProvider(),
                evaluationContext.getRuleProvider()).evaluateRoom(portfolio, trialSecurity,
                        3_000_000);
        assertEquals(1_000_000, room, TradeEvaluator.ROOM_TOLERANCE, "unexpected room result");

        // perform the same test with the clustered evaluator

        security1 = TestUtil.createTestSecurity(assetCache, "TradeEvaluatorTestSec3",
                security1Attributes);
        position1 = TestUtil.createTestPosition(assetCache, 1_000_000, security1);
        p1Positions = Set.of(position1);
        rule1 = TestUtil.createTestWeightedAverageRule(assetCache, null,
                "weighted average: duration <= 3.5", null, SecurityAttribute.duration, null, 3.5,
                null);
        Map<RuleKey, ? extends ConfigurableRule> cachedP1Rules = Map.of(rule1.getKey(), rule1);
        trialSecurity = TestUtil.createTestSecurity(assetCache, "TradeEvaluatorTestSec4",
                security2Attributes);
        portfolio = TestUtil.createTestPortfolio(assetCache, null, "test 1", p1Positions, null,
                cachedP1Rules.values());

        room = new TradeEvaluator(clusterEvaluator, assetCache, assetCache, assetCache)
                .evaluateRoom(portfolio, trialSecurity, 3_000_000);
        assertEquals(1_000_000, room, TradeEvaluator.ROOM_TOLERANCE, "unexpected room result");
    }
}
