package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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

        assertEquals(key, deserialized, "deserialized value should equals() original value");
    }
}