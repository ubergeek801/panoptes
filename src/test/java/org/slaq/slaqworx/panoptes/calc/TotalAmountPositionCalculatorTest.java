package org.slaq.slaqworx.panoptes.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
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
        positions.add(new MaterializedPosition(100, dummySecurity.getKey()));
        positions.add(new MaterializedPosition(200, dummySecurity.getKey()));
        positions.add(new MaterializedPosition(300, dummySecurity.getKey()));
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        double totalAmount = calculator.calculate(portfolio,
                new EvaluationContext(null, securityProvider, null));
        // the total should merely be the sum of the amounts
        assertEquals(600, totalAmount, TestUtil.EPSILON, "unexpected total amount");
    }
}
