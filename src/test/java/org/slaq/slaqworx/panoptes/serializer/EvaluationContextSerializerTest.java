package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.SecurityAttributes;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;

/**
 * {@code EvaluationContextSerializerTest} tests the functionality of the
 * {@code EvaluationContextSerializer}.
 *
 * @author jeremy
 */
public class EvaluationContextSerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        EvaluationContextSerializer serializer = new EvaluationContextSerializer();

        Map<SecurityKey, SecurityAttributes> securityAttributeOverrides =
                Map.of(TestUtil.s1.getKey(), TestUtil.s1.getAttributes(), TestUtil.s2.getKey(),
                        TestUtil.s2.getAttributes());
        EvaluationContext context = new EvaluationContext(EvaluationMode.SHORT_CIRCUIT_EVALUATION,
                securityAttributeOverrides);

        byte[] buffer = serializer.write(context);
        EvaluationContext deserialized = serializer.read(buffer);

        assertEquals(context, deserialized, "deserialized value should equals() original value");
        assertEquals(context.getEvaluationMode(), deserialized.getEvaluationMode(),
                "deserialized value should have evaluation mode equal to original");
        Map<SecurityKey, SecurityAttributes> deserializedOverrides =
                deserialized.getSecurityOverrides();
        assertEquals(securityAttributeOverrides.size(), deserializedOverrides.size(),
                "deserialized value should have same number of overrides as original");
        SecurityAttributes deserializedS1Attributes =
                deserializedOverrides.get(TestUtil.s1.getKey());
        assertEquals(TestUtil.s1.getAttributes(), deserializedS1Attributes,
                "deserialized value should have same s1 overrides as original");
        SecurityAttributes deserializedS2Attributes =
                deserializedOverrides.get(TestUtil.s2.getKey());
        assertEquals(TestUtil.s2.getAttributes(), deserializedS2Attributes,
                "deserialized value should have same s2 overrides as original");
    }
}
