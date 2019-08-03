package org.slaq.slaqworx.panoptes.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
        assertTrue("Position of 2MM should have passed",
                filter1MM.test(new PositionEvaluationContext(p1, evaluationContext)));

        MaterializedPosition p2 = new MaterializedPosition(500_000, TestUtil.s1.getKey());
        assertFalse("Position of 500K should not have passed",
                filter1MM.test(new PositionEvaluationContext(p2, evaluationContext)));

        // create a filter which should include Positions in a Security with a moovyRating > 88
        GroovyPositionFilter filterMoovy88 = new GroovyPositionFilter(
                "s.getAttributeValue(SecurityAttribute.of(\"Moovy\")) > 88");

        MaterializedPosition p3 = new MaterializedPosition(1_000_000, TestUtil.s1.getKey());
        assertTrue("Position with 90 rating should have passed",
                filterMoovy88.test(new PositionEvaluationContext(p3, evaluationContext)));

        MaterializedPosition p4 = new MaterializedPosition(1_000_000, TestUtil.s2.getKey());
        assertFalse("Position with 85 rating should not have passed",
                filterMoovy88.test(new PositionEvaluationContext(p4, evaluationContext)));

        // a simplified version of the above
        filterMoovy88 = new GroovyPositionFilter("s.Moovy > 88");
        assertTrue("Position with 90 rating should have passed",
                filterMoovy88.test(new PositionEvaluationContext(p3, evaluationContext)));
        assertFalse("Position with 85 rating should not have passed",
                filterMoovy88.test(new PositionEvaluationContext(p4, evaluationContext)));

        // create a filter which should include Positions in a Security with a country = "NZ"
        GroovyPositionFilter filterCountryNZ = new GroovyPositionFilter("s.country == \"NZ\"");
        assertFalse("Position with country US should not have passed",
                filterCountryNZ.test(new PositionEvaluationContext(p3, evaluationContext)));
        assertTrue("Position with country NZ should have passed",
                filterCountryNZ.test(new PositionEvaluationContext(p4, evaluationContext)));
    }
}
