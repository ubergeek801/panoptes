package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.SecurityAttributes;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * Tests the functionality of the {@link EvaluationContextSerializer}.
 *
 * @author jeremy
 */
public class EvaluationContextSerializerTest {
  /**
   * Tests that (de)serialization works as expected.
   *
   * @throws Exception if an unexpected error occurs
   */
  @Test
  public void testSerialization() throws Exception {
    EvaluationContextSerializer serializer = new EvaluationContextSerializer();

    Map<SecurityKey, SecurityAttributes> securityAttributeOverrides =
        Map.of(
            TestUtil.s1.getKey(),
            TestUtil.s1.getAttributes(),
            TestUtil.s2.getKey(),
            TestUtil.s2.getAttributes());
    EvaluationContext context =
        new EvaluationContext(EvaluationMode.SHORT_CIRCUIT_EVALUATION, securityAttributeOverrides);

    byte[] buffer = serializer.write(context);
    EvaluationContext deserialized = serializer.read(buffer);

    // note that EvaluationContext.equals() uses identity semantics, so an equals() test is
    // inappropriate

    assertEquals(
        context.getEvaluationMode(),
        deserialized.getEvaluationMode(),
        "deserialized value should have evaluation mode equal to original");
    Map<SecurityKey, SecurityAttributes> deserializedOverrides =
        deserialized.getSecurityOverrides();
    assertEquals(
        securityAttributeOverrides.size(),
        deserializedOverrides.size(),
        "deserialized value should have same number of overrides as original");
    SecurityAttributes deserializedS1Attributes = deserializedOverrides.get(TestUtil.s1.getKey());
    assertEquals(
        TestUtil.s1.getAttributes(),
        deserializedS1Attributes,
        "deserialized value should have same s1 overrides as original");
    SecurityAttributes deserializedS2Attributes = deserializedOverrides.get(TestUtil.s2.getKey());
    assertEquals(
        TestUtil.s2.getAttributes(),
        deserializedS2Attributes,
        "deserialized value should have same s2 overrides as original");
  }
}
