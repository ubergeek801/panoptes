package org.slaq.slaqworx.panoptes.cache;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * Tests the functionality of the {@link EligibilityResolver}.
 *
 * @author jeremy
 */
@MicronautTest
public class EligibilityResolverTest {
  @Inject
  private AssetCache assetCache;

  /**
   * Tests that country list eligibility behaves as expected.
   */
  @Test
  public void testCountryList() {
    EligibilityResolver resolver = new EligibilityResolver(assetCache);

    Map<SecurityAttribute<?>, ? super Object> attributes =
        Map.of(SecurityAttribute.isin, "testIsin", SecurityAttribute.cusip, "testCusip",
            SecurityAttribute.country, "testCountry1");
    Security security = new Security(attributes);

    assetCache.getEligibilityCache().set("testCountryList", Set.of("testCountry1"));
    assertTrue(resolver.isCountryListMember(security, "testCountryList"),
        "security should be a member of specified list");

    assetCache.getEligibilityCache().set("testCountryList", Set.of("testCountry2"));
    assertFalse(resolver.isCountryListMember(security, "testCountryList"),
        "security should not be a member of specified list");
  }

  /**
   * Tests that issuer list eligibility behaves as expected.
   */
  @Test
  public void testIssuerList() {
    EligibilityResolver resolver = new EligibilityResolver(assetCache);

    Map<SecurityAttribute<?>, ? super Object> attributes =
        Map.of(SecurityAttribute.isin, "testIsin", SecurityAttribute.cusip, "testCusip",
            SecurityAttribute.issuer, "testIssuer1");
    Security security = new Security(attributes);

    assetCache.getEligibilityCache().set("testIssuerList", Set.of("testIssuer1"));
    assertTrue(resolver.isIssuerListMember(security, "testIssuerList"),
        "security should be a member of specified list");

    assetCache.getEligibilityCache().set("testIssuerList", Set.of("testIssuer2"));
    assertFalse(resolver.isIssuerListMember(security, "testIssuerList"),
        "security should not be a member of specified list");
  }

  /**
   * Tests that security list eligibility behaves as expected.
   */
  @Test
  public void testSecurityList() {
    EligibilityResolver resolver = new EligibilityResolver(assetCache);

    Map<SecurityAttribute<?>, ? super Object> attributes =
        Map.of(SecurityAttribute.isin, "testIsin1", SecurityAttribute.cusip, "testCusip1");
    Security security = new Security(attributes);

    assetCache.getEligibilityCache().set("testSecurityList", Set.of("testIsin1"));
    assertTrue(resolver.isSecurityListMember(security, "testSecurityList"),
        "security should be a member of specified list");

    assetCache.getEligibilityCache().set("testSecurityList", Set.of("testCusip1"));
    assertTrue(resolver.isSecurityListMember(security, "testSecurityList"),
        "security should be a member of specified list");

    assetCache.getEligibilityCache().set("testSecurityList", Set.of("testIsin2"));
    assertFalse(resolver.isSecurityListMember(security, "testSecurityList"),
        "security should not be a member of specified list");
  }
}
