package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * GroovyPositionFilterTest tests the functionality of the GroovyPositionFilter.
 *
 * @author jeremy
 */
public class GroovyPositionFilterTest {
    @Test
    public void testFilter() {
        EvaluationContext evaluationContext = new EvaluationContext();

        // create a filter which should include Positions with an amount > 1MM
        GroovyPositionFilter filter1MM = GroovyPositionFilter.of("p.amount > 1_000_000");

        Position p1 = new Position(2_000_000, TestUtil.s1);
        assertTrue(filter1MM.test(new PositionEvaluationContext(p1, evaluationContext)),
                "Position of 2MM should have passed");

        Position p2 = new Position(500_000, TestUtil.s1);
        assertFalse(filter1MM.test(new PositionEvaluationContext(p2, evaluationContext)),
                "Position of 500K should not have passed");

        // create a filter which should include Positions in a Security with a moovyRating > 88
        GroovyPositionFilter filterMoovy88 = GroovyPositionFilter
                .of("s.getAttributeValue(SecurityAttribute.of('Moovy'), ctx) > 88");

        Position p3 = new Position(1_000_000, TestUtil.s1);
        assertTrue(filterMoovy88.test(new PositionEvaluationContext(p3, evaluationContext)),
                "Position with 90 rating should have passed");

        Position p4 = new Position(1_000_000, TestUtil.s2);
        assertFalse(filterMoovy88.test(new PositionEvaluationContext(p4, evaluationContext)),
                "Position with 85 rating should not have passed");

        // a simplified version of the above
        filterMoovy88 = GroovyPositionFilter.of("s.Moovy > 88");
        assertTrue(filterMoovy88.test(new PositionEvaluationContext(p3, evaluationContext)),
                "Position with 90 rating should have passed");
        assertFalse(filterMoovy88.test(new PositionEvaluationContext(p4, evaluationContext)),
                "Position with 85 rating should not have passed");

        // create a filter which should include Positions in a Security with a country = "NZ"
        GroovyPositionFilter filterCountryNZ = GroovyPositionFilter.of("s.country == 'NZ'");
        assertFalse(filterCountryNZ.test(new PositionEvaluationContext(p3, evaluationContext)),
                "Position with country US should not have passed");
        assertTrue(filterCountryNZ.test(new PositionEvaluationContext(p4, evaluationContext)),
                "Position with country NZ should have passed");

        // create a filter which should include Positions in a Security with a country = "US" or
        // country = "NZ"

        Position p5 = new Position(1_000_000, TestUtil.s3);
        GroovyPositionFilter filterCountryUSorNZ =
                GroovyPositionFilter.of("s.country == 'US' || s.country == 'NZ'");
        assertFalse(filterCountryUSorNZ.test(new PositionEvaluationContext(p5, evaluationContext)),
                "Position with country CA should not have passed");
        assertTrue(filterCountryUSorNZ.test(new PositionEvaluationContext(p3, evaluationContext)),
                "Position with country US should have passed");
        assertTrue(filterCountryUSorNZ.test(new PositionEvaluationContext(p4, evaluationContext)),
                "Position with country NZ should have passed");
    }
}
