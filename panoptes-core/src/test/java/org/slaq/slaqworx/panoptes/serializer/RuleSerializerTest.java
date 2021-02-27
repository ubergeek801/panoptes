package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.GroovyPositionFilter;
import org.slaq.slaqworx.panoptes.rule.MarketValueRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.TopNSecurityAttributeAggregator;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * {@code RuleSerializerTest} tests the functionality of the {@code RuleSerializer}.
 *
 * @author jeremy
 */
public class RuleSerializerTest {
  /**
   * Tests that (de)serialization works as expected.
   *
   * @throws Exception
   *     if an unexpected error occurs
   */
  @Test
  public void testSerialization() throws Exception {
    RuleSerializer serializer = new RuleSerializer();

    ConfigurableRule rule = new ConcentrationRule(new RuleKey("foo"), "test rule",
        GroovyPositionFilter.of("p.amount > 1_000_000"), 1d, 2d,
        new TopNSecurityAttributeAggregator(SecurityAttribute.country, 5));

    byte[] buffer = serializer.write(rule);
    ConfigurableRule deserialized = serializer.read(buffer);

    assertEquals(rule, deserialized, "deserialized value should equals() original value");
    assertEquals(rule.getDescription(), deserialized.getDescription(),
        "deserialized value should have same description as original");
    assertEquals(rule.getGroovyFilter(), deserialized.getGroovyFilter(),
        "deserialized value should have same filter as original");
    assertEquals(rule.getJsonConfiguration(), deserialized.getJsonConfiguration(),
        "deserialized value should have same configuration as original");
    if (rule.getGroupClassifier() != null
        && rule.getGroupClassifier() instanceof JsonConfigurable) {
      assertNotNull(deserialized.getGroupClassifier(),
          "deserialized value should have group classifier");
      assertTrue(deserialized.getGroupClassifier() instanceof JsonConfigurable,
          "deserialized group classifier should implement JsonConfigurable");
      assertEquals(((JsonConfigurable) rule.getGroupClassifier()).getJsonConfiguration(),
          ((JsonConfigurable) deserialized.getGroupClassifier()).getJsonConfiguration(),
          "deserialized group classifier should have same configuration as original");
    }

    rule = new WeightedAverageRule<>(new RuleKey("foo"), "test rule", null,
        SecurityAttribute.duration, 1d, 2d,
        new TopNSecurityAttributeAggregator(SecurityAttribute.duration, 10));

    buffer = serializer.write(rule);
    deserialized = serializer.read(buffer);

    assertEquals(rule, deserialized, "deserialized value should equals() original value");
    assertEquals(rule.getDescription(), deserialized.getDescription(),
        "deserialized value should have same description as original");
    assertNull(deserialized.getGroovyFilter(), "deserialized value should have null filter");
    assertEquals(rule.getJsonConfiguration(), deserialized.getJsonConfiguration(),
        "deserialized value should have same configuration as original");
    if (rule.getGroupClassifier() != null
        && rule.getGroupClassifier() instanceof JsonConfigurable) {
      assertNotNull(deserialized.getGroupClassifier(),
          "deserialized value should have group classifier");
      assertTrue(deserialized.getGroupClassifier() instanceof JsonConfigurable,
          "deserialized group classifier should implement JsonConfigurable");
      assertEquals(((JsonConfigurable) rule.getGroupClassifier()).getJsonConfiguration(),
          ((JsonConfigurable) deserialized.getGroupClassifier()).getJsonConfiguration(),
          "deserialized group classifier should have same configuration as original");
    }

    rule = new MarketValueRule(new RuleKey("foo"), "test rule", null, 1d, 2d);

    buffer = serializer.write(rule);
    deserialized = serializer.read(buffer);

    assertEquals(rule, deserialized, "deserialized value should equals() original value");
    assertEquals(rule.getDescription(), deserialized.getDescription(),
        "deserialized value should have same description as original");
    assertNull(deserialized.getGroovyFilter(), "deserialized value should have null filter");
    assertEquals(rule.getJsonConfiguration(), deserialized.getJsonConfiguration(),
        "deserialized value should have same configuration as original");
  }
}
