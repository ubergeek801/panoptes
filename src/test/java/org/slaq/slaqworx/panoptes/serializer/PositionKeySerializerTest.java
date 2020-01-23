package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.asset.PositionKey;

/**
 * PositionKeySerializerTest tests the functionality of the PositionKeySerializer.
 *
 * @author jeremy
 */
public class PositionKeySerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        PositionKeySerializer serializer = new PositionKeySerializer();

        PositionKey key = new PositionKey("foo");
        byte[] buffer = serializer.write(key);
        PositionKey deserialized = serializer.read(buffer);

        assertEquals(key, deserialized, "deserialized value should equals() original value");
    }
}