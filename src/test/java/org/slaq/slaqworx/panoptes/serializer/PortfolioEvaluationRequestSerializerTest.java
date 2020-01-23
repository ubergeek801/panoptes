package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * {@code PortfolioEvaluationRequestSerializerTest} tests the functionality of the
 * {@code PortfolioEvaluationRequestSerializer}.
 *
 * @author jeremy
 */
public class PortfolioEvaluationRequestSerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        PortfolioEvaluationRequestSerializer serializer = new PortfolioEvaluationRequestSerializer(
                TestUtil.testPortfolioProvider(), TestUtil.testSecurityProvider());

        Position p1 = new Position(100d, TestUtil.s1);
        Position p2 = new Position(200d, TestUtil.s2);
        Transaction t1 = new Transaction(TestUtil.p1.getKey(), List.of(p1, p2));
        Portfolio portfolio = TestUtil.p1;
        EvaluationContext evaluationContext = new EvaluationContext(EvaluationMode.FULL_EVALUATION);
        evaluationContext.setPortfolioMarketValue(100_000_000d);

        PortfolioEvaluationRequest request =
                new PortfolioEvaluationRequest(portfolio, t1, evaluationContext);

        byte[] buffer = serializer.write(request);
        PortfolioEvaluationRequest deserialized = serializer.read(buffer);

        assertEquals(request, deserialized, "deserialized value should equals() original value");
        assertEquals(request.getPortfolioKey(), deserialized.getPortfolioKey(),
                "deserialized value should have same Portfolio key as original");
        assertEquals(request.getEvaluationContext(), deserialized.getEvaluationContext(),
                "deserialized value should have evaluation context equal to original");
        assertEquals(request.getTransaction(), deserialized.getTransaction(),
                "deserialized value should have transaction equal to original");

        // perform same test but with null Transaction

        request = new PortfolioEvaluationRequest(portfolio, null, evaluationContext);

        buffer = serializer.write(request);
        deserialized = serializer.read(buffer);

        assertEquals(request, deserialized, "deserialized value should equals() original value");
        assertEquals(request.getPortfolioKey(), deserialized.getPortfolioKey(),
                "deserialized value should have same Portfolio key as original");
        assertEquals(request.getEvaluationContext(), deserialized.getEvaluationContext(),
                "deserialized value should have evaluation context equal to original");
    }
}