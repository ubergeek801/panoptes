package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.test.TestUtil;
import org.slaq.slaqworx.panoptes.trade.TaxLot;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * Tests the functionality of the {@link PortfolioEvaluationRequestSerializer}.
 *
 * @author jeremy
 */
public class PortfolioEvaluationRequestSerializerTest {
  /**
   * Tests that (de)serialization works as expected.
   *
   * @throws Exception if an unexpected error occurs
   */
  @Test
  public void testSerialization() throws Exception {
    PortfolioEvaluationRequestSerializer serializer = new PortfolioEvaluationRequestSerializer();

    TaxLot p1 = new TaxLot(100d, TestUtil.s1.getKey());
    TaxLot p2 = new TaxLot(200d, TestUtil.s2.getKey());
    Transaction t1 = new Transaction(TestUtil.p1.getKey(), List.of(p1, p2));
    Portfolio portfolio = TestUtil.p1;
    EvaluationContext evaluationContext = TestUtil.defaultTestEvaluationContext();

    PortfolioEvaluationRequest request =
        new PortfolioEvaluationRequest(portfolio.getKey(), t1, evaluationContext);

    byte[] buffer = serializer.write(request);
    PortfolioEvaluationRequest deserialized = serializer.read(buffer);

    assertEquals(request, deserialized, "deserialized value should equals() original value");
    assertEquals(
        request.getPortfolioKey(),
        deserialized.getPortfolioKey(),
        "deserialized value should have same Portfolio key as original");

    // note that EvaluationContext.equals() uses identity semantics, so an equals() test is
    // inappropriate
    assertEquals(
        evaluationContext.getEvaluationMode(),
        deserialized.getEvaluationContext().getEvaluationMode(),
        "deserialized EvaluationContext should have evaluation mode equal to original");

    assertEquals(
        request.getTransaction(),
        deserialized.getTransaction(),
        "deserialized value should have transaction equal to original");

    // perform same test but with null Transaction

    request = new PortfolioEvaluationRequest(portfolio.getKey(), null, evaluationContext);

    buffer = serializer.write(request);
    deserialized = serializer.read(buffer);

    assertEquals(request, deserialized, "deserialized value should equals() original value");
    assertEquals(
        request.getPortfolioKey(),
        deserialized.getPortfolioKey(),
        "deserialized value should have same Portfolio key as original");
    assertEquals(
        evaluationContext.getEvaluationMode(),
        deserialized.getEvaluationContext().getEvaluationMode(),
        "deserialized EvaluationContext should have evaluation mode equal to original");

    assertNull(deserialized.getTransaction(), "deserialized value should have null transaction");
  }
}
