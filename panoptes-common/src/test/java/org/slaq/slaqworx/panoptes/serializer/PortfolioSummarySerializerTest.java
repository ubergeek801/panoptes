package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * Tests the functionality of the {@link PortfolioSummarySerializer}.
 *
 * @author jeremy
 */
public class PortfolioSummarySerializerTest {
  /**
   * Tests that (de)serialization works as expected.
   *
   * @throws Exception if an unexpected error occurs
   */
  @Test
  public void testSerialization() throws Exception {
    EvaluationContext evaluationContext = TestUtil.defaultTestEvaluationContext();
    PortfolioSummarySerializer serializer = new PortfolioSummarySerializer();

    PortfolioSummary portfolioSummary =
        PortfolioSummary.fromPortfolio(TestUtil.p1, evaluationContext);
    byte[] buffer = serializer.write(portfolioSummary);
    PortfolioSummary deserialized = serializer.read(buffer);

    assertEquals(
        portfolioSummary, deserialized, "deserialized value should equals() original value");
    assertEquals(
        portfolioSummary.benchmarkKey(),
        deserialized.benchmarkKey(),
        "deserialized value should have same benchmark key as original");
    assertEquals(
        portfolioSummary.name(),
        deserialized.name(),
        "deserialized value should have same name as original");
    assertEquals(
        portfolioSummary.totalMarketValue(),
        deserialized.totalMarketValue(),
        "deserialized Portfolio should have same total market value as original");
    assertEquals(
        portfolioSummary.isAbstract(),
        deserialized.isAbstract(),
        "deserialized Portfolio should have same abstract flag as original");
  }
}
