package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;
import org.slaq.slaqworx.panoptes.test.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * Tests the functionality of {@link Security}.
 *
 * @author jeremy
 */
@MicronautTest
public class SecurityTest {
  @Inject
  private AssetCache assetCache;

  /**
   * Tests that {@code getAttributes()} behaves as expected.
   */
  @Test
  public void testGetAttributes() {
    TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

    Security s = securityProvider.newSecurity("dummy", SecurityAttribute
        .mapOf(SecurityAttribute.country, "US", SecurityAttribute.cusip, "abcde",
            SecurityAttribute.duration, 3.1, SecurityAttribute.coupon, 4d,
            SecurityAttribute.maturityDate, LocalDate.of(2019, 8, 5), SecurityAttribute.price, 1d));
    SecurityAttributes attributes = s.getAttributes();
    assertEquals("US", attributes.getValue(SecurityAttribute.country),
        "country value should have matched");
    assertEquals("abcde", attributes.getValue(SecurityAttribute.cusip),
        "cusip value should have matched");
    assertEquals(3.1, attributes.getValue(SecurityAttribute.duration), TestUtil.EPSILON,
        "duration value should have matched");
    assertEquals(4d, attributes.getValue(SecurityAttribute.coupon), TestUtil.EPSILON,
        "coupon value should have matched");
    assertEquals(LocalDate.of(2019, 8, 5), attributes.getValue(SecurityAttribute.maturityDate),
        "maturity date value should have matched");
  }

  /**
   * Tests that {@code getAttributeValue()} behaves as expected.
   */
  @Test
  public void testGetAttributeValue() {
    Map<SecurityAttribute<?>, ? super Object> attributes =
        SecurityAttribute.mapOf(SecurityAttribute.isin, "foo", SecurityAttribute.duration, 4d);
    Security security = TestUtil.createTestSecurity(assetCache, "foo", attributes);
    assertEquals("foo", security.getAttributeValue(SecurityAttribute.isin),
        "unexpected value for isin");
    assertEquals(4d, security.getAttributeValue(SecurityAttribute.duration),
        "unexpected value for duration");
  }

  /**
   * Tests that {@code getEffectiveAttributeValue()} behaves as expected.
   */
  @Test
  public void testGetEffectiveAttributeValue() {
    Map<SecurityAttribute<?>, ? super Object> attributes =
        SecurityAttribute.mapOf(SecurityAttribute.isin, "foo", SecurityAttribute.duration, 4d);
    Security security = TestUtil.createTestSecurity(assetCache, "foo", attributes);

    // test some overridden attribute values
    Map<SecurityKey, SecurityAttributes> overrides = Map.of(security.getKey(),
        new SecurityAttributes(SecurityAttribute
            .mapOf(SecurityAttribute.duration, 3d, SecurityAttribute.country, "US")));
    EvaluationContext evaluationContext =
        new EvaluationContext(EvaluationMode.FULL_EVALUATION, overrides);
    assertEquals(3d,
        security.getEffectiveAttributeValue(SecurityAttribute.duration, evaluationContext),
        "expected overridden value for duration");
    // country didn't exist in the original Security but that shouldn't matter
    assertEquals("US",
        security.getEffectiveAttributeValue(SecurityAttribute.country, evaluationContext),
        "expected overridden value for country");
  }

  /**
   * Tests that {@link Security} objects are hashed in a reasonable way.
   */
  @Test
  public void testHash() {
    TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();

    Security s1 = securityProvider.newSecurity("s1", SecurityAttribute
        .mapOf(SecurityAttribute.country, "US", SecurityAttribute.cusip, "abcde",
            SecurityAttribute.price, 99.1234));
    Security s2 = securityProvider.newSecurity("s2", SecurityAttribute
        .mapOf(SecurityAttribute.cusip, "abcde", SecurityAttribute.currency, "USD",
            SecurityAttribute.duration, 3d));
    Security s3 = securityProvider.newSecurity("s3", SecurityAttribute
        .mapOf(SecurityAttribute.description, "a security", SecurityAttribute.price, 99.1));
    Security s4 = securityProvider.newSecurity("s4", Collections.emptyMap());
    // these are the same as above, with the attributes permuted; these should hash to the same
    Security s1a = securityProvider.newSecurity("s1", SecurityAttribute
        .mapOf(SecurityAttribute.cusip, "abcde", SecurityAttribute.price, 99.1234,
            SecurityAttribute.country, "US"));
    Security s2a = securityProvider.newSecurity("s2", SecurityAttribute
        .mapOf(SecurityAttribute.cusip, "abcde", SecurityAttribute.duration, 3d,
            SecurityAttribute.currency, "USD"));
    Security s3a = securityProvider.newSecurity("s3", SecurityAttribute
        .mapOf(SecurityAttribute.price, 99.1, SecurityAttribute.description, "a security"));
    Security s4a = securityProvider.newSecurity("s4", Collections.emptyMap());

    HashSet<Security> securities = new HashSet<>();
    // adding the four distinct Securities any number of times should still result in four
    // distinct Securities
    securities.add(s1);
    securities.add(s2);
    securities.add(s3);
    securities.add(s4);
    securities.add(s1a);
    securities.add(s2a);
    securities.add(s3a);
    securities.add(s4a);
    securities.add(s1);
    securities.add(s2);
    securities.add(s4);
    securities.add(s2);
    securities.add(s4);
    securities.add(s1);
    securities.add(s3);

    assertEquals(4, securities.size(), "unexpected number of Securities");
    assertTrue(securities.contains(s1), "Securities should have contained s1");
    assertTrue(securities.contains(s2), "Securities should have contained s2");
    assertTrue(securities.contains(s3), "Securities should have contained s3");
    assertTrue(securities.contains(s4), "Securities should have contained s4");
  }
}
