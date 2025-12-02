package org.slaq.slaqworx.panoptes.pipeline;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.operators.co.KeyedCoProcessOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedTwoInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.TestHarnessUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;

/**
 * Tests the functionality of the {@link BenchmarkComparator}.
 *
 * @author jeremy
 */
public class BenchmarkComparatorTest {
  private KeyedTwoInputStreamOperatorTestHarness<
          PortfolioRuleKey, RuleEvaluationResult, RuleEvaluationResult, RuleEvaluationResult>
      harness;

  @BeforeEach
  public void setup() throws Exception {
    BenchmarkComparator comparator = new BenchmarkComparator();
    KeyedCoProcessOperator<
            PortfolioRuleKey, RuleEvaluationResult, RuleEvaluationResult, RuleEvaluationResult>
        comparatorOperator = new KeyedCoProcessOperator<>(comparator);
    harness =
        new KeyedTwoInputStreamOperatorTestHarness<>(
            comparatorOperator,
            RuleEvaluationResult::getBenchmarkEvaluationKey,
            RuleEvaluationResult::getBenchmarkEvaluationKey,
            TypeInformation.of(PortfolioRuleKey.class));
    harness.setup();
    harness.open();
  }

  /**
   * Tests that the operator behaves as expected when a benchmark result is received before the
   * corresponding portfolio result.
   *
   * @throws Exception if an unexpected failure occurs
   */
  @Test
  public void testBenchmarkBeforePortfolio() throws Exception {
    PortfolioKey benchmarkKey = new PortfolioKey("benchmark", 1);
    RuleKey ruleKey = new RuleKey("rule");
    Map<EvaluationGroup, ValueResult> benchmarkEvaluationResults =
        Map.of(EvaluationGroup.defaultGroup(), new ValueResult(Threshold.WITHIN, 1));
    EvaluationResult benchmarkResult = new EvaluationResult(ruleKey, benchmarkEvaluationResults);
    RuleEvaluationResult benchmarkResultInput =
        new RuleEvaluationResult(
            0, benchmarkKey, null, EvaluationSource.BENCHMARK, false, null, null, benchmarkResult);

    PortfolioKey portfolioKey = new PortfolioKey("portfolio", 1);
    Map<EvaluationGroup, ValueResult> portfolioEvaluationResults =
        Map.of(EvaluationGroup.defaultGroup(), new ValueResult(Threshold.WITHIN, 2));
    EvaluationResult portfolioResult = new EvaluationResult(ruleKey, portfolioEvaluationResults);
    RuleEvaluationResult portfolioResultInput =
        new RuleEvaluationResult(
            0,
            portfolioKey,
            benchmarkKey,
            EvaluationSource.PORTFOLIO,
            true,
            0d,
            5d,
            portfolioResult);

    harness.processElement2(benchmarkResultInput, 0L);
    harness.processElement1(portfolioResultInput, 1L);

    // actual output type is RuleEvaluationResult
    Queue<Object> output = harness.getOutput();

    Queue<Object> expectedOutput = new LinkedList<>();
    StreamRecord<RuleEvaluationResult> expectedResult =
        new StreamRecord<>(
            new RuleEvaluationResult(
                0,
                portfolioKey,
                benchmarkKey,
                EvaluationSource.BENCHMARK_COMPARISON,
                true,
                0d,
                5d,
                new EvaluationResult(ruleKey, portfolioEvaluationResults)),
            1L);
    expectedOutput.add(expectedResult);

    TestHarnessUtil.assertOutputEquals(
        "actual output did not match expected output", expectedOutput, output);
  }

  /**
   * Tests that the operator behaves as expected when a portfolio result is received that has no
   * related benchmark.
   *
   * @throws Exception if an unexpected failure occurs
   */
  @Test
  public void testNonBenchmarkSupportedRule() throws Exception {
    PortfolioKey portfolioKey = new PortfolioKey("portfolio", 1);
    RuleKey ruleKey = new RuleKey("rule");
    // must be a Kryo-supported Map
    HashMap<EvaluationGroup, ValueResult> portfolioEvaluationResults = new HashMap<>();
    portfolioEvaluationResults.put(
        EvaluationGroup.defaultGroup(), new ValueResult(Threshold.WITHIN, 2));
    EvaluationResult portfolioResult = new EvaluationResult(ruleKey, portfolioEvaluationResults);
    RuleEvaluationResult portfolioResultInput =
        new RuleEvaluationResult(
            0, portfolioKey, null, EvaluationSource.PORTFOLIO, true, 0d, 5d, portfolioResult);

    harness.processElement1(portfolioResultInput, 0L);

    // actual output type is EvaluationResult
    Queue<Object> output = harness.getOutput();

    Queue<Object> expectedOutput = new LinkedList<>();
    StreamRecord<RuleEvaluationResult> expectedResult =
        new StreamRecord<>(
            new RuleEvaluationResult(
                0,
                portfolioKey,
                null,
                EvaluationSource.PORTFOLIO,
                true,
                0d,
                5d,
                new EvaluationResult(ruleKey, portfolioEvaluationResults)),
            0L);
    expectedOutput.add(expectedResult);

    TestHarnessUtil.assertOutputEquals(
        "actual output did not match expected output", expectedOutput, output);
  }

  /**
   * Tests that the operator behaves as expected when a portfolio result is received before the
   * corresponding benchmark result.
   *
   * @throws Exception if an unexpected failure occurs
   */
  @Test
  public void testPortfolioBeforeBenchmark() throws Exception {
    PortfolioKey benchmarkKey = new PortfolioKey("benchmark", 1);
    RuleKey ruleKey = new RuleKey("rule");
    Map<EvaluationGroup, ValueResult> benchmarkEvaluationResults =
        Map.of(EvaluationGroup.defaultGroup(), new ValueResult(Threshold.WITHIN, 1));
    EvaluationResult benchmarkResult = new EvaluationResult(ruleKey, benchmarkEvaluationResults);
    RuleEvaluationResult benchmarkResultInput =
        new RuleEvaluationResult(
            0, benchmarkKey, null, EvaluationSource.BENCHMARK, false, null, null, benchmarkResult);

    PortfolioKey portfolioKey = new PortfolioKey("portfolio", 1);
    Map<EvaluationGroup, ValueResult> portfolioEvaluationResults =
        Map.of(EvaluationGroup.defaultGroup(), new ValueResult(Threshold.WITHIN, 2));
    EvaluationResult portfolioResult = new EvaluationResult(ruleKey, portfolioEvaluationResults);
    RuleEvaluationResult portfolioResultInput =
        new RuleEvaluationResult(
            0,
            portfolioKey,
            benchmarkKey,
            EvaluationSource.PORTFOLIO,
            true,
            0d,
            5d,
            portfolioResult);

    harness.processElement1(portfolioResultInput, 0L);
    harness.processElement2(benchmarkResultInput, 1L);

    // actual output type is EvaluationResult
    Queue<Object> output = harness.getOutput();

    Queue<Object> expectedOutput = new LinkedList<>();
    StreamRecord<RuleEvaluationResult> expectedResult =
        new StreamRecord<>(
            new RuleEvaluationResult(
                0,
                portfolioKey,
                benchmarkKey,
                EvaluationSource.BENCHMARK_COMPARISON,
                true,
                0d,
                5d,
                new EvaluationResult(ruleKey, portfolioEvaluationResults)),
            1L);
    expectedOutput.add(expectedResult);

    TestHarnessUtil.assertOutputEquals(
        "actual output did not match expected output", expectedOutput, output);
  }
}
