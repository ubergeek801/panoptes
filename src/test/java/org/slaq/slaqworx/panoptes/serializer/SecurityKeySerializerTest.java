package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * SecurityKeySerializerTest tests the functionality of the SecurityKeySerializer.
 *
 * @author jeremy
 */
public class SecurityKeySerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        SecurityKeySerializer serializer = new SecurityKeySerializer();

        SecurityKey key = new SecurityKey("foo");
        byte[] buffer = serializer.write(key);
        SecurityKey deserialized = serializer.read(buffer);

        assertEquals(key, deserialized, "deserialized value should equals() original value");
    }
}
