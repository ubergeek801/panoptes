package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import org.junit.jupiter.api.Test;

/**
 * Tests the functionality of {@link SecurityAttributes}.
 *
 * @author jeremy
 */
public class SecurityAttributesTest {
  /** Tests that {@code asSortedMap()} behaves as expected. */
  @Test
  public void testAsSortedMap() {
    Map<SecurityAttribute<?>, ? super Object> attributes =
        SecurityAttribute.mapOf(
            SecurityAttribute.isin,
            "foo",
            SecurityAttribute.amount,
            100d,
            SecurityAttribute.duration,
            4d,
            SecurityAttribute.country,
            "US",
            SecurityAttribute.issuer,
            "slaq",
            SecurityAttribute.price,
            100d);
    SecurityAttributes securityAttributes = new SecurityAttributes(attributes);
    SortedMap<SecurityAttribute<?>, Object> sortedAttributes = securityAttributes.asSortedMap();
    // an Iterator should return the keys in sorted order
    Iterator<SecurityAttribute<?>> sortedAttributeIterator = sortedAttributes.keySet().iterator();
    assertEquals(
        SecurityAttribute.amount, sortedAttributeIterator.next(), "unexpected element order");
    assertEquals(
        SecurityAttribute.country, sortedAttributeIterator.next(), "unexpected element order");
    assertEquals(
        SecurityAttribute.duration, sortedAttributeIterator.next(), "unexpected element order");
    assertEquals(
        SecurityAttribute.isin, sortedAttributeIterator.next(), "unexpected element order");
    assertEquals(
        SecurityAttribute.issuer, sortedAttributeIterator.next(), "unexpected element order");
    assertEquals(
        SecurityAttribute.price, sortedAttributeIterator.next(), "unexpected element order");
  }
}
