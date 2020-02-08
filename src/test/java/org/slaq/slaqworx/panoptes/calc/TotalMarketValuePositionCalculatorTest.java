package org.slaq.slaqworx.panoptes.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;

/**
 * {@code TotalMarketValuePositionCalculatorTest} tests the functionality of the
 * {@code TotalMarketValuePositionCalculator}.
 *
 * @author jeremy
 */
public class TotalMarketValuePositionCalculatorTest {
    /**
     * Tests that {@code calculate()} behaves as expected.
     */
    @Test
    public void testCalculate() {
        TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

        TotalMarketValuePositionCalculator calculator = new TotalMarketValuePositionCalculator();
        Security dummySecurity = securityProvider.newSecurity("dummy",
                Map.of(SecurityAttribute.price, new BigDecimal("2.00")));

        HashSet<Position> positions = new HashSet<>();
        positions.add(new SimplePosition(100, dummySecurity.getKey()));
        positions.add(new SimplePosition(200, dummySecurity.getKey()));
        positions.add(new SimplePosition(300, dummySecurity.getKey()));
        Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);

        double totalMarketValue = calculator.calculate(
                portfolio.getPositionsWithContext(TestUtil.defaultTestEvaluationContext()));
        // the total should merely be the sum of the (amounts * price)
        assertEquals(1200, totalMarketValue, TestUtil.EPSILON, "unexpected total market value");
    }
}
