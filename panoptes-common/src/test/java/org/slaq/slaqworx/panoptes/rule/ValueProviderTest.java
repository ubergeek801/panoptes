package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Tests the functionality of the {@link ValueProvider}.
 *
 * @author jeremy
 */
public class ValueProviderTest {
  /** Tests that a {@link ValueProvider} for {@link BigDecimal} behaves as expected. */
  @Test
  public void testValueProvider_BigDecimal() {
    ValueProvider<BigDecimal> valueProvider = ValueProvider.forBigDecimal();
    assertEquals(
        1.5, valueProvider.apply(new BigDecimal("1.5"), null), "unexpected value from provider");
    valueProvider = ValueProvider.forClass(BigDecimal.class);
    assertEquals(
        1.5, valueProvider.apply(new BigDecimal("1.5"), null), "unexpected value from provider");
    assertNull(valueProvider.apply(null, null), "null input should always produce null output");
  }

  /** Tests that a {@link ValueProvider} for {@link Double} behaves as expected. */
  @Test
  public void testValueProvider_Double() {
    ValueProvider<Double> valueProvider = ValueProvider.forDouble();
    assertEquals(1.5, valueProvider.apply(1.5, null), "unexpected value from provider");
    valueProvider = ValueProvider.forClass(Double.class);
    assertEquals(1.5, valueProvider.apply(1.5, null), "unexpected value from provider");
    assertNull(valueProvider.apply(null, null), "null input should always produce null output");
  }

  /** Tests that a {@link ValueProvider} for {@link LocalDate} behaves as expected. */
  @Test
  public void testValueProvider_LocalDate() {
    // this test could theoretically break if run right at midnight
    LocalDate now = LocalDate.now();
    LocalDate oneWeekLater = now.plusDays(7);
    ValueProvider<LocalDate> valueProvider = ValueProvider.forDaysUntilDate();
    assertEquals(7, valueProvider.apply(oneWeekLater, null), "unexpected value from provider");
    valueProvider = ValueProvider.forClass(LocalDate.class);
    assertEquals(7, valueProvider.apply(oneWeekLater, null), "unexpected value from provider");
    assertNull(valueProvider.apply(null, null), "null input should always produce null output");
  }

  /** Tests that a {@link ValueProvider} for rating symbols/notches behaves as expected. */
  @Test
  public void testValueProvider_RatingSymbol() {
    ValueProvider<String> valueProvider = ValueProvider.forRatingSymbol();
    // we are not concerned with the exact notch indexes of AAA and AA2, but they should be two
    // notches apart
    Double aaaNotch = valueProvider.apply("AAA", null);
    Double aa2Notch = valueProvider.apply("AA2", null);
    assertEquals(2, aaaNotch - aa2Notch, "unexpected value(s) from provider");
    valueProvider = ValueProvider.forClass(String.class);
    aaaNotch = valueProvider.apply("AAA", null);
    aa2Notch = valueProvider.apply("AA2", null);
    assertEquals(2, aaaNotch - aa2Notch, "unexpected value(s) from provider");
    assertNull(valueProvider.apply(null, null), "null input should always produce null output");
  }

  /** Tests that {@link ValueProvider} behaves as expected for an unsupported type. */
  @Test
  public void testValueProvider_unsupported() {
    try {
      ValueProvider.forClass(Exception.class);
      fail("ValueProvider should have thrown exception for unsupported class");
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }
}
