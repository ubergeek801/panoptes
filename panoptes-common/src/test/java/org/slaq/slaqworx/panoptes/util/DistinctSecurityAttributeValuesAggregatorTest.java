package org.slaq.slaqworx.panoptes.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * Tests the functionality of the {@link DistinctSecurityAttributeValuesAggregator}.
 *
 * @author jeremy
 */
public class DistinctSecurityAttributeValuesAggregatorTest {
  /** Tests that {@code aggregate()} behaves as expected. */
  @Test
  public void testAggregate() {
    Security us1 =
        new Security(
            SecurityAttribute.mapOf(
                SecurityAttribute.isin, "us1", SecurityAttribute.country, "US"));
    Security us2 =
        new Security(
            SecurityAttribute.mapOf(
                SecurityAttribute.isin, "us2", SecurityAttribute.country, "US"));
    Security us3 =
        new Security(
            SecurityAttribute.mapOf(
                SecurityAttribute.isin, "us3", SecurityAttribute.country, "US"));
    Security nz1 =
        new Security(
            SecurityAttribute.mapOf(
                SecurityAttribute.isin, "nz1", SecurityAttribute.country, "NZ"));
    Security nz2 =
        new Security(
            SecurityAttribute.mapOf(
                SecurityAttribute.isin, "nz2", SecurityAttribute.country, "NZ"));
    Security nullCountry = new Security(SecurityAttribute.mapOf(SecurityAttribute.isin, "null"));
    Map<SecurityKey, Security> securities =
        List.of(us1, us2, us3, nz1, nz2, nullCountry).stream()
            .collect(Collectors.toMap(s -> s.getKey(), s -> s));

    DistinctSecurityAttributeValuesAggregator<String> aggregator =
        new DistinctSecurityAttributeValuesAggregator<>(SecurityAttribute.country);

    securities.entrySet().forEach(e -> aggregator.accumulate(e));
    SortedSet<String> distinctValues = aggregator.aggregate();

    // distinct values do not include null, so we expect to find US and NZ
    assertEquals(2, distinctValues.size(), "unexpected number of distinct values");
    assertTrue(distinctValues.contains("US"), "distinct values should contain 'US'");
    assertTrue(distinctValues.contains("NZ"), "distinct values should contain 'NZ'");

    // try explicitly calling combine() as well

    DistinctSecurityAttributeValuesAggregator<String> aggregator1 =
        new DistinctSecurityAttributeValuesAggregator<>(SecurityAttribute.country);
    DistinctSecurityAttributeValuesAggregator<String> aggregator2 =
        new DistinctSecurityAttributeValuesAggregator<>(SecurityAttribute.country);

    Map<SecurityKey, Security> securities1 =
        List.of(us1, us2, us3, nullCountry).stream()
            .collect(Collectors.toMap(s -> s.getKey(), s -> s));
    Map<SecurityKey, Security> securities2 =
        List.of(nz1, nz2).stream().collect(Collectors.toMap(s -> s.getKey(), s -> s));

    securities1.entrySet().forEach(e -> aggregator1.accumulate(e));
    securities2.entrySet().forEach(e -> aggregator2.accumulate(e));
    aggregator2.combine(aggregator1);
    distinctValues = aggregator2.aggregate();

    // the ultimate result should be the same as above
    assertEquals(2, distinctValues.size(), "unexpected number of distinct values");
    assertTrue(distinctValues.contains("US"), "distinct values should contain 'US'");
    assertTrue(distinctValues.contains("NZ"), "distinct values should contain 'NZ'");
  }
}
