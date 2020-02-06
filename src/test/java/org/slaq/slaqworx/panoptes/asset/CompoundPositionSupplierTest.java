package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * {@code CompoundPositionSupplierTest} tests the functionality of the
 * {@code CompoundPositionSupplier}.
 *
 * @author jeremy
 */
public class CompoundPositionSupplierTest {
    /**
     * Tests that {@code PositionSupplier} concatenation behaves as expected.
     */
    @Test
    public void testConcat() {
        EvaluationContext evaluationContext = TestUtil.defaultTestEvaluationContext;

        PortfolioKey portfolioKey = new PortfolioKey("test", 1);
        PositionSupplier s1 = new PositionSet<>(TestUtil.p1Positions, portfolioKey);
        PositionSupplier s2 = new PositionSet<>(TestUtil.p2Positions, portfolioKey);
        PositionSupplier s3 = new PositionSet<>(TestUtil.p3Positions, portfolioKey);
        PositionSupplier concat = PositionSupplier.concat(s1, s2, s3);

        // there should be a total of 7 positions (2 + 2 + 3)
        List<Position> positions = concat.getPositions().collect(Collectors.toList());
        assertEquals(7, positions.size(),
                "number of positions should equal sum of position set sizes");

        assertEquals(portfolioKey, concat.getPortfolioKey(),
                "portfolio key should equal that of the portfolios");

        // total market value should be 1000 + 500 + 500 + 1000 + 500 + 1000 + 200 = 4700
        assertEquals(4700, concat.getTotalMarketValue(evaluationContext),
                "total market value should equal sum of position sets");
    }
}
