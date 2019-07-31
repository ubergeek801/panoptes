package org.slaq.slaqworx.panoptes.calc;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * TotalAmountPositionCalculatorTest tests the functionality of the TotalAmountPositionCalculator.
 *
 * @author jeremy
 */
public class TotalAmountPositionCalculatorTest {
    /**
     * Tests that calculate() behaves as expected.
     */
    @Test
    public void testCalculate() {
        TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

        TotalAmountPositionCalculator calculator = new TotalAmountPositionCalculator();
        Security dummySecurity = securityProvider.newSecurity(Collections.emptyMap());

        HashSet<Position> positions = new HashSet<>();
        positions.add(new Position(100, dummySecurity));
        positions.add(new Position(200, dummySecurity));
        positions.add(new Position(300, dummySecurity));
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), positions);

        double totalAmount =
                calculator.calculate(portfolio, new EvaluationContext(null, securityProvider));
        // the total should merely be the sum of the amounts
        assertEquals("unexpected total amount", 600, totalAmount, TestUtil.EPSILON);
    }
}
