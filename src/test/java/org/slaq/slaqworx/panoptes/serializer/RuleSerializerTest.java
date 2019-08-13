package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.GroovyPositionFilter;
import org.slaq.slaqworx.panoptes.rule.MaterializedRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.TopNSecurityAttributeAggregator;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * RuleSerializerTest tests the functionality of the RuleSerializer.
 *
 * @author jeremy
 */
public class RuleSerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        RuleSerializer serializer = new RuleSerializer();

        MaterializedRule rule = new ConcentrationRule(new RuleKey("foo"), "test rule",
                new GroovyPositionFilter("p.amount > 1_000_000"), 1d, 2d,
                new TopNSecurityAttributeAggregator(TestUtil.country, 5));

        byte[] buffer = serializer.write(rule);
        MaterializedRule deserialized = serializer.read(buffer);

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
            assertEquals(((JsonConfigurable)rule.getGroupClassifier()).getJsonConfiguration(),
                    ((JsonConfigurable)deserialized.getGroupClassifier()).getJsonConfiguration(),
                    "deserialized group classifier should have same configuration as original");
        }

        rule = new WeightedAverageRule(new RuleKey("foo"), "test rule",
                new GroovyPositionFilter("p.amount > 1_000_000"), TestUtil.duration, 1d, 2d,
                new TopNSecurityAttributeAggregator(TestUtil.duration, 10));

        buffer = serializer.write(rule);
        deserialized = serializer.read(buffer);

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
            assertEquals(((JsonConfigurable)rule.getGroupClassifier()).getJsonConfiguration(),
                    ((JsonConfigurable)deserialized.getGroupClassifier()).getJsonConfiguration(),
                    "deserialized group classifier should have same configuration as original");
        }
    }
}
