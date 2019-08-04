package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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

        assertEquals("deserialized value should equals() original value", rule, deserialized);
        assertEquals("deserialized value should have same description as original",
                rule.getDescription(), deserialized.getDescription());
        assertEquals("deserialized value should have same filter as original",
                rule.getGroovyFilter(), deserialized.getGroovyFilter());
        assertEquals("deserialized value should have same configuration as original",
                rule.getJsonConfiguration(), deserialized.getJsonConfiguration());
        if (rule.getGroupClassifier() != null
                && rule.getGroupClassifier() instanceof JsonConfigurable) {
            assertNotNull("deserialized value should have group classifier",
                    deserialized.getGroupClassifier());
            assertTrue("deserialized group classifier should implement JsonConfigurable",
                    deserialized.getGroupClassifier() instanceof JsonConfigurable);
            assertEquals("deserialized group classifier should have same configuration as original",
                    ((JsonConfigurable)rule.getGroupClassifier()).getJsonConfiguration(),
                    ((JsonConfigurable)deserialized.getGroupClassifier()).getJsonConfiguration());
        }

        rule = new WeightedAverageRule(new RuleKey("foo"), "test rule",
                new GroovyPositionFilter("p.amount > 1_000_000"), TestUtil.duration, 1d, 2d,
                new TopNSecurityAttributeAggregator(TestUtil.duration, 10));

        buffer = serializer.write(rule);
        deserialized = serializer.read(buffer);

        assertEquals("deserialized value should equals() original value", rule, deserialized);
        assertEquals("deserialized value should have same description as original",
                rule.getDescription(), deserialized.getDescription());
        assertEquals("deserialized value should have same filter as original",
                rule.getGroovyFilter(), deserialized.getGroovyFilter());
        assertEquals("deserialized value should have same configuration as original",
                rule.getJsonConfiguration(), deserialized.getJsonConfiguration());
        if (rule.getGroupClassifier() != null
                && rule.getGroupClassifier() instanceof JsonConfigurable) {
            assertNotNull("deserialized value should have group classifier",
                    deserialized.getGroupClassifier());
            assertTrue("deserialized group classifier should implement JsonConfigurable",
                    deserialized.getGroupClassifier() instanceof JsonConfigurable);
            assertEquals("deserialized group classifier should have same configuration as original",
                    ((JsonConfigurable)rule.getGroupClassifier()).getJsonConfiguration(),
                    ((JsonConfigurable)deserialized.getGroupClassifier()).getJsonConfiguration());
        }
    }
}
