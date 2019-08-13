package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;

/**
 * GroovyPositionFilterTest tests the functionality of the GroovyPositionFilter.
 *
 * @author jeremy
 */
public class GroovyPositionFilterTest {
    @Test
    public void testFilter() {
        EvaluationContext evaluationContext =
                new EvaluationContext(null, TestUtil.testSecurityProvider(), null);

        // create a filter which should include Positions with an amount > 1MM
        GroovyPositionFilter filter1MM = new GroovyPositionFilter("p.amount > 1_000_000");

        MaterializedPosition p1 = new MaterializedPosition(2_000_000, TestUtil.s1.getKey());
        assertTrue(filter1MM.test(new PositionEvaluationContext(p1, evaluationContext)),
                "Position of 2MM should have passed");

        MaterializedPosition p2 = new MaterializedPosition(500_000, TestUtil.s1.getKey());
        assertFalse(filter1MM.test(new PositionEvaluationContext(p2, evaluationContext)),
                "Position of 500K should not have passed");

        // create a filter which should include Positions in a Security with a moovyRating > 88
        GroovyPositionFilter filterMoovy88 = new GroovyPositionFilter(
                "s.getAttributeValue(SecurityAttribute.of(\"Moovy\")) > 88");

        MaterializedPosition p3 = new MaterializedPosition(1_000_000, TestUtil.s1.getKey());
        assertTrue(filterMoovy88.test(new PositionEvaluationContext(p3, evaluationContext)),
                "Position with 90 rating should have passed");

        MaterializedPosition p4 = new MaterializedPosition(1_000_000, TestUtil.s2.getKey());
        assertFalse(filterMoovy88.test(new PositionEvaluationContext(p4, evaluationContext)),
                "Position with 85 rating should not have passed");

        // a simplified version of the above
        filterMoovy88 = new GroovyPositionFilter("s.Moovy > 88");
        assertTrue(filterMoovy88.test(new PositionEvaluationContext(p3, evaluationContext)),
                "Position with 90 rating should have passed");
        assertFalse(filterMoovy88.test(new PositionEvaluationContext(p4, evaluationContext)),
                "Position with 85 rating should not have passed");

        // create a filter which should include Positions in a Security with a country = "NZ"
        GroovyPositionFilter filterCountryNZ = new GroovyPositionFilter("s.country == \"NZ\"");
        assertFalse(filterCountryNZ.test(new PositionEvaluationContext(p3, evaluationContext)),
                "Position with country US should not have passed");
        assertTrue(filterCountryNZ.test(new PositionEvaluationContext(p4, evaluationContext)),
                "Position with country NZ should have passed");

        // create a filter which should include Positions in a Security with a country = "US" or
        // country = "NZ"

        MaterializedPosition p5 = new MaterializedPosition(1_000_000, TestUtil.s3.getKey());
        GroovyPositionFilter filterCountryUSorNZ =
                new GroovyPositionFilter("s.country == \"US\" || s.country == \"NZ\"");
        assertFalse(filterCountryUSorNZ.test(new PositionEvaluationContext(p5, evaluationContext)),
                "Position with country CA should not have passed");
        assertTrue(filterCountryUSorNZ.test(new PositionEvaluationContext(p3, evaluationContext)),
                "Position with country US should have passed");
        assertTrue(filterCountryUSorNZ.test(new PositionEvaluationContext(p4, evaluationContext)),
                "Position with country NZ should have passed");
    }
}
