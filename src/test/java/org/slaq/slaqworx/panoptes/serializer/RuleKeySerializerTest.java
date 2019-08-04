package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * RuleKeySerializerTest tests the functionality of the RuleKeySerializer.
 *
 * @author jeremy
 */
public class RuleKeySerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        RuleKeySerializer serializer = new RuleKeySerializer();

        RuleKey key = new RuleKey("foo");
        byte[] buffer = serializer.write(key);
        RuleKey deserialized = serializer.read(buffer);

        assertEquals("deserialized value should equals() original value", key, deserialized);
    }
}
