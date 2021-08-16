package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * Tests the functionality of the {@link RuleEvaluationResultSerializer}.
 *
 * @author jeremy
 */
public class RuleEvaluationResultSerializerTest {
  /**
   * Tests that (de)serialization works as expected.
   *
   * @throws Exception
   *     if an unexpected error occurs
   */
  @Test
  public void testSerialization() throws Exception {
    RuleEvaluationResultSerializer serializer = new RuleEvaluationResultSerializer();

    long eventId = 31337;
    PortfolioKey portfolioKey = new PortfolioKey("testPortfolio", 1);
    PortfolioKey benchmarkKey = new PortfolioKey("testBenchmark", 1);
    EvaluationSource source = EvaluationSource.PORTFOLIO;
    boolean isBenchmarkSupported = true;
    Double lowerLimit = 1d;
    Double upperLimit = 2d;
    RuleKey ruleKey = new RuleKey("testRule");
    EvaluationResult evaluationResult = new EvaluationResult(ruleKey, Collections.emptyMap());
    RuleEvaluationResult result =
        new RuleEvaluationResult(eventId, portfolioKey, benchmarkKey, source,
            isBenchmarkSupported, lowerLimit, upperLimit, evaluationResult);

    byte[] buffer = serializer.write(result);
    RuleEvaluationResult deserialized = serializer.read(buffer);

    assertEquals(result, deserialized, "deserialized result should equals() original");
  }
}
