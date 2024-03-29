package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * Tests the functionality of the {@link GroovyPositionFilter}.
 *
 * @author jeremy
 */
@MicronautTest
public class GroovyPositionFilterTest {
  @Inject private AssetCache assetCache;

  /** Tests that basic filtering behaves as expected. */
  @Test
  public void testFilter_basic() {
    // create a filter which should include Positions with an amount > 1MM
    GroovyPositionFilter filter1MM = GroovyPositionFilter.of("p.amount > 1_000_000");

    Position p1 = new SimplePosition(2_000_000, TestUtil.s1.getKey());
    assertTrue(
        filter1MM.test(new PositionEvaluationContext(p1, TestUtil.defaultTestEvaluationContext())),
        "Position of 2MM should have passed");

    Position p2 = new SimplePosition(500_000, TestUtil.s1.getKey());
    assertFalse(
        filter1MM.test(new PositionEvaluationContext(p2, TestUtil.defaultTestEvaluationContext())),
        "Position of 500K should not have passed");

    // create a filter which should include Positions in a Security with a moovyRating > 88
    GroovyPositionFilter filterMoovy88 =
        GroovyPositionFilter.of(
            "s.getEffectiveAttributeValue(SecurityAttribute.of('Moovy'), ctx) > 88");

    Position p3 = new SimplePosition(1_000_000, TestUtil.s1.getKey());
    assertTrue(
        filterMoovy88.test(
            new PositionEvaluationContext(p3, TestUtil.defaultTestEvaluationContext())),
        "Position with 90 rating should have passed");

    Position p4 = new SimplePosition(1_000_000, TestUtil.s2.getKey());
    assertFalse(
        filterMoovy88.test(
            new PositionEvaluationContext(p4, TestUtil.defaultTestEvaluationContext())),
        "Position with 85 rating should not have passed");

    // a simplified version of the above
    filterMoovy88 = GroovyPositionFilter.of("s.Moovy > 88");
    assertTrue(
        filterMoovy88.test(
            new PositionEvaluationContext(p3, TestUtil.defaultTestEvaluationContext())),
        "Position with 90 rating should have passed");
    assertFalse(
        filterMoovy88.test(
            new PositionEvaluationContext(p4, TestUtil.defaultTestEvaluationContext())),
        "Position with 85 rating should not have passed");

    // create a filter which should include Positions in a Security with a country = "NZ"
    GroovyPositionFilter filterCountryNZ = GroovyPositionFilter.of("s.country == 'NZ'");
    assertFalse(
        filterCountryNZ.test(
            new PositionEvaluationContext(p3, TestUtil.defaultTestEvaluationContext())),
        "Position with country US should not have passed");
    assertTrue(
        filterCountryNZ.test(
            new PositionEvaluationContext(p4, TestUtil.defaultTestEvaluationContext())),
        "Position with country NZ should have passed");

    // create a filter which should include Positions in a Security with a country = "US" or
    // country = "NZ"

    Position p5 = new SimplePosition(1_000_000, TestUtil.s3.getKey());
    GroovyPositionFilter filterCountryUSorNZ =
        GroovyPositionFilter.of("s.country == 'US' || s.country == 'NZ'");
    assertFalse(
        filterCountryUSorNZ.test(
            new PositionEvaluationContext(p5, TestUtil.defaultTestEvaluationContext())),
        "Position with country CA should not have passed");
    assertTrue(
        filterCountryUSorNZ.test(
            new PositionEvaluationContext(p3, TestUtil.defaultTestEvaluationContext())),
        "Position with country US should have passed");
    assertTrue(
        filterCountryUSorNZ.test(
            new PositionEvaluationContext(p4, TestUtil.defaultTestEvaluationContext())),
        "Position with country NZ should have passed");
  }

  /** Tests that filtering on eligibility lists behaves as expected. */
  @Test
  public void testFilter_eligibilityList() {
    GroovyPositionFilter filter = GroovyPositionFilter.of("s.inCountryList(\"test1\")");
    TestUtil.createTestEligibilityList("test1", Set.of("US", "CA"));

    // this security's country is on the list and thus should pass the filter
    Security usSecurity =
        TestUtil.createTestSecurity(
            assetCache, "usTestSecurity", Map.of(SecurityAttribute.country, "US"));
    Position usPosition = TestUtil.createTestPosition(assetCache, 1_000_000, usSecurity.getKey());

    assertTrue(
        filter.test(
            new PositionEvaluationContext(usPosition, TestUtil.defaultTestEvaluationContext())),
        "Position with country US should have passed");

    // this security's country is not on the list and thus should not pass the filter
    Security nzSecurity =
        TestUtil.createTestSecurity(
            assetCache, "nzTestSecurity", Map.of(SecurityAttribute.country, "NZ"));
    Position nzPosition = TestUtil.createTestPosition(assetCache, 1_000_000, nzSecurity.getKey());

    assertFalse(
        filter.test(
            new PositionEvaluationContext(nzPosition, TestUtil.defaultTestEvaluationContext())),
        "Position with country NZ should not have passed");
  }
}
