package org.slaq.slaqworx.panoptes.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.Lists;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.operators.co.CoBroadcastWithKeyedOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedTwoInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.TestHarnessUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.GenericRule;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;

/**
 * Tests the functionality of the {@code BenchmarkRuleEvaluator}.
 *
 * @author jeremy
 */
public class BenchmarkRuleEvaluatorTest {
    private BenchmarkRuleEvaluator evaluator;
    private KeyedTwoInputStreamOperatorTestHarness<PortfolioKey, PortfolioEvent, Security,
            RuleEvaluationResult> harness;

    @BeforeEach
    public void setup() throws Exception {
        evaluator = new BenchmarkRuleEvaluator();
        CoBroadcastWithKeyedOperator<PortfolioKey, PortfolioEvent, Security,
                RuleEvaluationResult> evaluatorOperator = new CoBroadcastWithKeyedOperator<>(
                        evaluator, List.of(PanoptesPipeline.SECURITY_STATE_DESCRIPTOR));
        harness =
                new KeyedTwoInputStreamOperatorTestHarness<>(evaluatorOperator,
                        ((PortfolioEvent e) -> e.getBenchmarkKey() != null ? e.getBenchmarkKey()
                                : e.getPortfolioKey()),
                        null, TypeInformation.of(PortfolioKey.class));
        harness.setup();
        harness.open();
    }

    /**
     * Tests that the operator behaves as expected when a benchmark portfolio is received before the
     * corresponding securities.
     *
     * @throws Exception
     *             if an unexpected failure occurs
     */
    @Test
    public void testBenchmarkBeforeSecurities() throws Exception {
        String isin1 = "security1";
        Security security1 = new Security(Map.of(SecurityAttribute.isin, isin1,
                SecurityAttribute.duration, 0d, SecurityAttribute.price, 1d));

        String isin2 = "security2";
        Security security2 = new Security(Map.of(SecurityAttribute.isin, isin2,
                SecurityAttribute.duration, 0d, SecurityAttribute.price, 1d));

        PortfolioKey benchmarkKey = new PortfolioKey("benchmark1", 0);
        SimplePosition benchmarkPosition1 = new SimplePosition(1, security1.getKey());
        SimplePosition benchmarkPosition2 = new SimplePosition(1, security2.getKey());
        Set<Position> benchmarkPositions = new HashSet<>();
        benchmarkPositions.add(benchmarkPosition1);
        benchmarkPositions.add(benchmarkPosition2);

        Portfolio benchmark = new Portfolio(benchmarkKey, "benchmark1", benchmarkPositions);
        PortfolioDataEvent benchmarkEvent = new PortfolioDataEvent(benchmark);

        PortfolioKey portfolioKey = new PortfolioKey("portfolio1", 0);
        Set<Position> portfolioPositions = new HashSet<>();
        ArrayList<Rule> portfolioRules = new ArrayList<>();
        // a WeightedAverageRule is benchmark-enabled
        RuleKey rule1Key = new RuleKey("rule1");
        WeightedAverageRule<Double> rule1 = new WeightedAverageRule<>(rule1Key, "rule1", null,
                SecurityAttribute.duration, 0d, 0d, null);
        portfolioRules.add(rule1);
        // a GenericRule is not benchmark-enabled
        RuleKey rule2Key = new RuleKey("rule2");
        GenericRule rule2 = new GenericRule(rule2Key, "rule2") {
            @Override
            protected ValueResult eval(PositionSupplier positions, EvaluationGroup evaluationGroup,
                    EvaluationContext evaluationContext) {
                return new ValueResult(true);
            }
        };
        portfolioRules.add(rule2);
        Portfolio portfolio = new Portfolio(portfolioKey, "portfolio1", portfolioPositions,
                benchmarkKey, portfolioRules);
        PortfolioDataEvent portfolioEvent = new PortfolioDataEvent(portfolio);

        harness.processElement2(security1, 0L);
        harness.processElement1(benchmarkEvent, 1L);
        harness.processElement1(portfolioEvent, 2L);

        Iterable<RuleKey> rulesState = evaluator.getBenchmarkRulesState().keys();
        ArrayList<RuleKey> ruleKeys = Lists.newArrayList(rulesState);
        assertEquals(1, ruleKeys.size(),
                "number of rules in state should equal number of benchmark-enabled rules from portfolio");
        assertEquals(rule1Key, ruleKeys.get(0),
                "rule from state should have matched WeightedAverageRule");

        // nothing should be emitted yet
        Queue<Object> expectedOutput = new LinkedList<>();
        Queue<Object> output = harness.getOutput();
        TestHarnessUtil.assertOutputEquals("output should not be produced for incomplete benchmark",
                expectedOutput, output);

        harness.processElement2(security2, 3L);

        // now that all securities are accounted for, the benchmark-enabled rules should have been
        // executed and their results emitted
        EvaluationResult expectedEvaluationResult = new EvaluationResult(rule1Key,
                Map.of(EvaluationGroup.defaultGroup(), new ValueResult(Threshold.WITHIN, 0)));
        output = harness.getOutput();
        // peek at the output so we can get the event ID
        @SuppressWarnings("unchecked") RuleEvaluationResult evaluationResult =
                ((StreamRecord<RuleEvaluationResult>)output.element()).getValue();
        StreamRecord<RuleEvaluationResult> expectedResult =
                new StreamRecord<>(new RuleEvaluationResult(evaluationResult.getEventId(),
                        benchmarkKey, null, true, 0d, 0d, expectedEvaluationResult), 3L);
        expectedOutput.add(expectedResult);
        TestHarnessUtil.assertOutputEquals("unexpected output", expectedOutput, output);
    }

    /**
     * Tests that the operator behaves as expected when securities are received before the
     * corresponding benchmark portfolio.
     *
     * @throws Exception
     *             if an unexpected failure occurs
     */
    @Test
    public void testSecuritiesBeforeBenchmark() throws Exception {
        String isin1 = "security1";
        Security security1 = new Security(Map.of(SecurityAttribute.isin, isin1,
                SecurityAttribute.duration, 0d, SecurityAttribute.price, 1d));

        String isin2 = "security2";
        Security security2 = new Security(Map.of(SecurityAttribute.isin, isin2,
                SecurityAttribute.duration, 0d, SecurityAttribute.price, 1d));

        PortfolioKey benchmarkKey = new PortfolioKey("benchmark1", 0);
        SimplePosition benchmarkPosition1 = new SimplePosition(1, security1.getKey());
        SimplePosition benchmarkPosition2 = new SimplePosition(1, security2.getKey());
        Set<Position> benchmarkPositions = new HashSet<>();
        benchmarkPositions.add(benchmarkPosition1);
        benchmarkPositions.add(benchmarkPosition2);

        Portfolio benchmark = new Portfolio(benchmarkKey, "benchmark1", benchmarkPositions);
        PortfolioDataEvent benchmarkEvent = new PortfolioDataEvent(benchmark);

        PortfolioKey portfolioKey = new PortfolioKey("portfolio1", 0);
        Set<Position> portfolioPositions = new HashSet<>();
        ArrayList<Rule> portfolioRules = new ArrayList<>();
        // a WeightedAverageRule is benchmark-enabled
        RuleKey rule1Key = new RuleKey("rule1");
        WeightedAverageRule<Double> rule1 = new WeightedAverageRule<>(rule1Key, "rule1", null,
                SecurityAttribute.duration, 0d, 0d, null);
        portfolioRules.add(rule1);
        // a GenericRule is not benchmark-enabled
        RuleKey rule2Key = new RuleKey("rule2");
        GenericRule rule2 = new GenericRule(rule2Key, "rule2") {
            @Override
            protected ValueResult eval(PositionSupplier positions, EvaluationGroup evaluationGroup,
                    EvaluationContext evaluationContext) {
                return new ValueResult(true);
            }
        };
        portfolioRules.add(rule2);
        Portfolio portfolio = new Portfolio(portfolioKey, "portfolio1", portfolioPositions,
                benchmarkKey, portfolioRules);
        PortfolioDataEvent portfolioEvent = new PortfolioDataEvent(portfolio);

        harness.processElement2(security1, 0L);
        harness.processElement2(security2, 1L);
        harness.processElement1(portfolioEvent, 2L);

        Iterable<RuleKey> rulesState = evaluator.getBenchmarkRulesState().keys();
        ArrayList<RuleKey> ruleKeys = Lists.newArrayList(rulesState);
        assertEquals(1, ruleKeys.size(),
                "number of rules in state should equal number of benchmark-enabled rules from portfolio");
        assertEquals(rule1Key, ruleKeys.get(0),
                "rule from state should have matched WeightedAverageRule");

        // nothing should be emitted yet
        Queue<Object> expectedOutput = new LinkedList<>();
        Queue<Object> output = harness.getOutput();
        TestHarnessUtil.assertOutputEquals("output should not be produced for incomplete benchmark",
                expectedOutput, output);

        harness.processElement1(benchmarkEvent, 3L);

        // now that the benchmark has been received, the benchmark-enabled rules should have been
        // executed and their results emitted
        EvaluationResult expectedEvaluationResult = new EvaluationResult(rule1Key,
                Map.of(EvaluationGroup.defaultGroup(), new ValueResult(Threshold.WITHIN, 0)));
        output = harness.getOutput();
        // peek at the output so we can get the event ID
        @SuppressWarnings("unchecked") RuleEvaluationResult evaluationResult =
                ((StreamRecord<RuleEvaluationResult>)output.element()).getValue();
        StreamRecord<RuleEvaluationResult> expectedResult =
                new StreamRecord<>(new RuleEvaluationResult(evaluationResult.getEventId(),
                        benchmarkKey, null, true, 0d, 0d, expectedEvaluationResult), 3L);
        expectedOutput.add(expectedResult);
        TestHarnessUtil.assertOutputEquals("unexpected output", expectedOutput, output);
    }
}
